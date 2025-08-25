package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.healthcheck.finder.PretixHealthcheck;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.usecase.ReloadOrdersUseCase;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.feature.pretix.objects.product.PretixProductBundle;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.feature.pretix.objects.product.usecase.ReloadProductsUseCase;
import net.furizon.backend.feature.pretix.objects.question.PretixOption;
import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.feature.pretix.objects.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.pretix.objects.quota.finder.PretixQuotaFinder;
import net.furizon.backend.feature.pretix.objects.quota.usecase.ReloadQuotaUseCase;
import net.furizon.backend.feature.pretix.objects.states.CountryData;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.feature.pretix.objects.states.usecase.FetchStatesByCountry;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.furizon.backend.infrastructure.pretix.PretixConst.QUESTIONS_ACCOUNT_USERID;
import static net.furizon.backend.infrastructure.pretix.PretixConst.QUESTIONS_DUPLICATE_DATA;
import static net.furizon.backend.infrastructure.pretix.PretixConst.QUESTIONS_USER_NOTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPretixInformation implements PretixInformation {
    @NotNull
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final EventFinder eventFinder;
    @NotNull
    private final PretixQuotaFinder quotaFinder;
    @NotNull
    private final PretixProductFinder pretixProductFinder;
    @NotNull
    private final PretixHealthcheck healthcheck;
    @NotNull
    private final PretixConfig pretixConfig;
    @NotNull
    private final FursuitConfig fursuitConfig;

    // *** CACHE
    @NotNull
    private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);
    @NotNull
    private final AtomicReference<List<Event>> otherEvents = new AtomicReference<>(null);

    //Event Struct

    //map id -> price * 100
    @NotNull
    private final Cache<Long, Long> itemIdToPrice = Caffeine.newBuilder().build();
    //map id -> bundles
    private final Cache<Long, List<PretixProductBundle>> itemIdToBundle = Caffeine.newBuilder().build();
    //map id -> quota
    private final Cache<Long, List<PretixQuota>> itemIdToQuota = Caffeine.newBuilder().build();

    //Contains tickets, memberships, sponsors, rooms
    @NotNull
    private final Cache<CacheItemTypes, Set<Long>> itemIdsCache = Caffeine.newBuilder().build();

    //Questions
    @NotNull
    private final AtomicReference<Long> questionUserId = new AtomicReference<>(-1L);
    @NotNull
    private final AtomicReference<Long> questionDuplicateData = new AtomicReference<>(-1L);
    @NotNull
    private final AtomicReference<Long> questionUserNotes = new AtomicReference<>(-1L);
    @NotNull
    private final Cache<Long, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<Long, String> questionIdToIdentifier = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<Long, List<PretixOption>> questionIdToOptions = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<String, Long> questionIdentifierToId = Caffeine.newBuilder().build();

    //Tickets
    @NotNull
    private final Cache<Long, Integer> dailyIdToDay = Caffeine.newBuilder().build(); //map id -> day idx

    //Sponsors
    @NotNull
    private final Cache<Long, Sponsorship> sponsorshipIdToType = Caffeine.newBuilder().build();

    //Extra days
    @NotNull
    private final Cache<Long, ExtraDays> extraDaysIdToDay = Caffeine.newBuilder().build();

    //Rooms
    //map id -> (capacity, hotelName)
    @NotNull
    private final Cache<Long, HotelCapacityPair> roomIdToInfo = Caffeine.newBuilder().build();
    //map id -> room names
    @NotNull
    private final Cache<Long, Map<String, String>> roomPretixItemIdToNames = Caffeine.newBuilder().build();
    //map (capacity, hotelName) -> earlyExtraDays item id
    @NotNull
    private final Cache<HotelCapacityPair, Long> roomIdToEarlyExtraDayItemId = Caffeine.newBuilder().build();
    //map (capacity, hotelName) -> lateExtraDays item id
    @NotNull
    private final Cache<HotelCapacityPair, Long> roomIdToLateExtraDayItemId = Caffeine.newBuilder().build();
    //map capacity -> List[id of room items with that capacity]
    @NotNull
    private final Cache<Short, List<Long>> roomCapacityToItemId = Caffeine.newBuilder().build();

    //Countries
    @NotNull
    private final Cache<String, List<PretixState>> statesOfCountry = Caffeine.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .build();

    @Override
    @PostConstruct
    public void reloadCacheAndOrders() {
        if (!pretixConfig.isEnableSync()) {
            log.warn("[PRETIX] Pretix synchronization has been disabled");
            loadCurrentEventFromDb();
            return;
        }
        healthcheck.waitForPretix();
        log.info("[PRETIX] Syncing pretix information and cache it");
        long start = System.currentTimeMillis();
        resetCache();
        reloadAllOrders();
        log.info("[PRETIX] Reloading cache and orders required {} ms", System.currentTimeMillis() - start);
    }

    @Override
    public @Nullable Long getItemPrice(long itemId, boolean ignoreCache, boolean subtractBundlesPrice) {
        if (ignoreCache) {
            var v = pretixProductFinder.fetchProductById(getCurrentEvent(), itemId);
            if (!v.isPresent()) {
                return null;
            }
            PretixProduct product = v.get();
            long value = PretixGenericUtils.fromStrPriceToLong(product.getPrice());
            List<PretixProductBundle> bundles = product.getBundles();
            lock.writeLock().lock();
            itemIdToPrice.put(itemId, value);
            itemIdToBundle.put(itemId, bundles);
            lock.writeLock().unlock();
            value -= bundles.stream().mapToLong(PretixProductBundle::getTotalPrice).sum();
            return value;
        } else {
            //When reloading the event we already cache the various prices, so there always be a value present
            lock.readLock().lock();
            Long value = itemIdToPrice.getIfPresent(itemId);
            List<PretixProductBundle> bundles = itemIdToBundle.getIfPresent(itemId);
            lock.readLock().unlock();
            if (bundles != null) {
                value -= bundles.stream().mapToLong(PretixProductBundle::getTotalPrice).sum();
            }
            return value;
        }
    }

    @Override
    public @Nullable List<PretixProductBundle> getBundlesForItem(long itemId) {
        lock.readLock().lock();
        List<PretixProductBundle> bundles = itemIdToBundle.getIfPresent(itemId);
        lock.readLock().unlock();
        return bundles;
    }

    @Override
    public @NotNull Set<Long> getRoomPretixIds() {
        lock.readLock().lock();
        Set<Long> v = new HashSet<Long>(roomIdToInfo.asMap().keySet());
        lock.readLock().unlock();
        return v;
    }

    @Override
    public @Nullable HotelCapacityPair getRoomInfoFromPretixItemId(long roomPretixItemId) {
        lock.readLock().lock();
        var v = roomIdToInfo.getIfPresent(roomPretixItemId);
        lock.readLock().unlock();
        return v;
    }

    @Override
    public @NotNull List<Long> getRoomItemIdsForCapacity(short capacity) {
        lock.readLock().lock();
        List<Long> v = roomCapacityToItemId.getIfPresent(capacity);
        lock.readLock().unlock();
        return v == null ? Collections.emptyList() : v;
    }

    @Override
    public @Nullable HotelCapacityPair getBiggestRoomCapacity() {
        HotelCapacityPair biggest = null;
        short maxCapacity = 0;
        lock.readLock().lock();
        for (HotelCapacityPair p : roomIdToInfo.asMap().values()) {
            if (p.capacity() > maxCapacity) {
                maxCapacity = p.capacity();
                biggest = p;
            }
        }
        lock.readLock().unlock();
        return biggest;
    }

    @NotNull
    @Override
    public Map<String, String> getRoomNamesFromRoomPretixItemId(long roomPretixItemId) {
        lock.readLock().lock();
        var v = roomPretixItemIdToNames.getIfPresent(roomPretixItemId);
        lock.readLock().unlock();
        return v == null ? new HashMap<>() : v;
    }

    @Nullable
    @Override
    public Long getExtraDayItemIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                  short capacity, @NotNull ExtraDays day) {
        return getExtraDayItemIdForHotelCapacity(new HotelCapacityPair(hotelName, roomInternalName, capacity), day);
    }
    @Nullable
    @Override
    public Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day) {
        Cache<HotelCapacityPair, Long> map = null;
        if (day == ExtraDays.EARLY) {
            map = roomIdToEarlyExtraDayItemId;
        } else if (day == ExtraDays.LATE) {
            map = roomIdToLateExtraDayItemId;
        }
        if (map == null) {
            return null;
        }
        lock.readLock().lock();
        Long ret = map.getIfPresent(pair);
        lock.readLock().unlock();
        return ret;
    }

    @NotNull
    @Override
    public List<PretixState> getStatesOfCountry(@NotNull String countryIsoCode) {
        //No cache lock needed!
        var ret = statesOfCountry.get(countryIsoCode, k -> useCaseExecutor.execute(FetchStatesByCountry.class, k));
        return ret != null ? ret : new LinkedList<>();
    }
    @Override
    public boolean isCountryValid(@NotNull String countryIsoCode) {
        return isRegionOfCountryValid(PretixConst.ALL_COUNTRIES_STATE_KEY, countryIsoCode);
    }
    @Override
    public boolean isRegionOfCountryValid(@NotNull String countryIsoCode, @NotNull String region) {
        List<PretixState> countries = getStatesOfCountry(countryIsoCode);
        for (PretixState state : countries) {
            if (state.getCode().equals(region)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isPhonePrefixValid(@NotNull String phonePrefix) {
        List<PretixState> countries = getStatesOfCountry(PretixConst.ALL_COUNTRIES_STATE_KEY);
        for (PretixState state : countries) {
            if (state instanceof CountryData && ((CountryData) state).getPhonePrefix().equals(phonePrefix)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Set<Long> getIdsForItemType(@NotNull CacheItemTypes type) {
        lock.readLock().lock();
        var v = itemIdsCache.getIfPresent(type);
        lock.readLock().unlock();
        return v == null ? new HashSet<>() : v;
    }

    @NotNull
    @Override
    public Event getCurrentEvent() {
        lock.readLock().lock();
        var v = currentEvent.get();
        lock.readLock().unlock();
        if (v == null) {
            log.error("Current event is not set! (null value found)");
            throw new IllegalStateException("No current event available");
        }
        return v;
    }
    @NotNull
    public List<Event> getOtherEvents() {
        lock.readLock().lock();
        var v = otherEvents.get();
        lock.readLock().unlock();
        return v == null ? Collections.emptyList() : v;
    }

    @Override
    public long getQuestionUserId() {
        lock.readLock().lock();
        var v = questionUserId.get();
        lock.readLock().unlock();
        return v;
    }

    @Override
    public long getQuestionDuplicateData() {
        lock.readLock().lock();
        var v = questionDuplicateData.get();
        lock.readLock().unlock();
        return v;
    }

    @Override
    public long getQuestionUserNotes() {
        lock.readLock().lock();
        var v = questionUserNotes.get();
        lock.readLock().unlock();
        return v;
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromId(long id) {
        lock.readLock().lock();
        var v = questionIdToType.getIfPresent(id);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier) {
        lock.readLock().lock();
        var v = questionIdToType.getIfPresent(questionIdentifierToId.getIfPresent(identifier));
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<String> getQuestionIdentifierFromId(long id) {
        lock.readLock().lock();
        var v = questionIdToIdentifier.getIfPresent(id);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @Override
    public @NotNull Optional<List<PretixOption>> getQuestionOptionsFromId(long id) {
        lock.readLock().lock();
        var v = questionIdToOptions.getIfPresent(id);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier) {
        lock.readLock().lock();
        var v = questionIdentifierToId.getIfPresent(identifier);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<Order> parseOrder(@NotNull PretixOrder pretixOrder, @NotNull Event event) {
        lock.readLock().lock();
        try {
            Integer cacheDay;
            ExtraDays cacheExtraDays;
            BiFunction<CacheItemTypes, @NotNull Long, @NotNull Boolean> checkItemId = (type, item) ->
                    Objects.requireNonNull(itemIdsCache.getIfPresent(type)).contains(item);

            long questionUserId = this.getQuestionUserId();
            long questionDuplicateData = this.getQuestionDuplicateData();

            boolean hasTicket = false; //If no ticket is found, we don't store the order at all
            boolean foundDuplicate = false;
            OrderStatus status = OrderStatus.get(pretixOrder.getStatus());

            Set<Integer> days = new HashSet<>();
            Sponsorship sponsorship = Sponsorship.NONE;
            ExtraDays extraDays = ExtraDays.NONE;
            List<PretixAnswer> answers = null;
            String hotelInternalName = null;
            String roomInternalName = null;
            String checkinSecret = null;
            Long pretixRoomItemId = null;
            long ticketPositionId = -1L;
            long ticketPosid = -1L;
            Long roomPositionId = null;
            Long roomPosid = null;
            Long earlyPositionId = null;
            Long latePositionId = null;
            boolean membership = false;
            short roomCapacity = 0;
            short extraFursuits = 0;
            Long userId = null;

            List<PretixPosition> positions = pretixOrder.getPositions();
            if (positions.isEmpty()) {
                status = OrderStatus.CANCELED;
            }

            positionLoop:
            for (PretixPosition position : positions) {
                if (position.isCanceled()) {
                    continue;
                }

                long itemId = position.getItemId();

                if (checkItemId.apply(CacheItemTypes.TICKETS, itemId)) {
                    hasTicket = true;
                    checkinSecret = position.getSecret();
                    ticketPositionId = position.getPositionId();
                    ticketPosid = position.getPositionPosid();
                    answers = position.getAnswers();
                    for (PretixAnswer answer : answers) {
                        long questionId = answer.getQuestionId();
                        var questionType = getQuestionTypeFromId(questionId);
                        if (questionType.isPresent()) {
                            if (questionType.get() == QuestionType.FILE) {
                                answer.setAnswer(PretixConst.QUESTIONS_FILE_KEEP);
                            }

                            if (questionId == questionUserId) {
                                String s = answer.getAnswer();
                                if (s != null && !s.isBlank()) {
                                    userId = Float.valueOf(s).longValue();
                                }

                            } else if (questionId == questionDuplicateData) {
                                String s = answer.getAnswer();
                                if (s == null || s.isBlank()) {
                                    log.warn("Order was already marked as duplicate. "
                                            + "Skipping it. Duplicate data: {}", s);
                                    foundDuplicate = true;
                                    break positionLoop;
                                }
                            }
                        }
                    }

                } else if ((cacheDay = dailyIdToDay.getIfPresent(itemId)) != null) {
                    days.add(cacheDay);

                } else if (checkItemId.apply(CacheItemTypes.MEMBERSHIP_CARDS, itemId)) {
                    membership = true;

                } else if (checkItemId.apply(CacheItemTypes.SPONSORSHIPS, itemId)) {
                    Sponsorship s = sponsorshipIdToType.getIfPresent(position.getVariationId());
                    if (s != null && s.ordinal() > sponsorship.ordinal()) {
                        sponsorship = s; //keep the best sponsorship
                    }

                } else if ((cacheExtraDays = extraDaysIdToDay.getIfPresent(itemId)) != null) {
                    if (cacheExtraDays == ExtraDays.EARLY) {
                        earlyPositionId = position.getPositionId();
                    } else if (cacheExtraDays == ExtraDays.LATE) {
                        latePositionId = position.getPositionId();
                    }
                    extraDays = ExtraDays.or(extraDays, cacheExtraDays);

                } else if (checkItemId.apply(CacheItemTypes.ROOMS, itemId)) {
                    //Set the room position and item id anyway, even if we have a NO_ROOM item
                    roomPositionId = position.getPositionId();
                    roomPosid = position.getPositionPosid();
                    pretixRoomItemId = itemId;
                    if (checkItemId.apply(CacheItemTypes.NO_ROOM_ITEM, itemId)) {
                        roomCapacity = 0;
                        hotelInternalName = null;
                        roomInternalName = null;
                    } else {
                        HotelCapacityPair room = roomIdToInfo.getIfPresent(itemId);
                        if (room != null) {
                            roomCapacity = room.capacity();
                            hotelInternalName = room.hotelInternalName();
                            roomInternalName = room.roomInternalName();
                        }
                    }

                } else if (checkItemId.apply(CacheItemTypes.EXTRA_FURSUITS, itemId)) {
                    if (extraFursuits < fursuitConfig.getMaxExtraFursuits() && extraFursuits < Short.MAX_VALUE) {
                        extraFursuits++;
                    }

                }
            }

            if (!hasTicket || foundDuplicate) {
                return Optional.empty();
            }

            return Optional.of(
                Order.builder()
                    .code(pretixOrder.getCode())
                    .orderStatus(status)
                    .sponsorship(sponsorship)
                    .extraDays(extraDays)
                    .dailyDays(days)
                    .roomCapacity(roomCapacity)
                    .pretixRoomItemId(pretixRoomItemId)
                    .hotelInternalName(hotelInternalName)
                    .roomInternalName(roomInternalName)
                    .pretixOrderSecret(pretixOrder.getSecret())
                    .checkinSecret(checkinSecret)
                    .hasMembership(membership)
                    .buyerEmail(pretixOrder.getEmail())
                    .buyerPhone(pretixOrder.getPhone())
                    .buyerUser(pretixOrder.getCustomer())
                    .buyerLocale(pretixOrder.getLocale())
                    .ticketPositionId(ticketPositionId)
                    .ticketPositionPosid(ticketPosid)
                    .roomPositionId(roomPositionId)
                    .roomPositionPosid(roomPosid)
                    .earlyPositionId(earlyPositionId)
                    .latePositionId(latePositionId)
                    .eventId(event.getId())
                    .orderOwnerUserId(userId)
                    .answers(answers, this)
                    .extraFursuits(extraFursuits)
                    .userFinder(userFinder)
                    .eventFinder(eventFinder)
                    .requireAttention(pretixOrder.isCheckinRequiresAttention())
                    .checkinText(pretixOrder.getCheckinText())
                    .internalComment(pretixOrder.getComment())
                    .build()
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public @Nullable List<PretixQuota> getQuotaFromItemId(long itemId) {
        lock.readLock().lock();
        var v = itemIdToQuota.getIfPresent(itemId);
        lock.readLock().unlock();
        return v;
    }

    //This is NOT cached!!
    @Override
    public @NotNull Optional<PretixQuotaAvailability> getSmallestAvailabilityFromItemId(long itemId) {
        List<PretixQuota> quota = getQuotaFromItemId(itemId);
        if (quota == null || quota.isEmpty()) {
            log.warn("Quota not found for item {}", itemId);
            return Optional.of(new PretixQuotaAvailability(
                    true,
                    null,
                    null,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L));
        }
        //We return only the smallest availability
        Event event = getCurrentEvent();
        PretixQuotaAvailability availability = null;
        for (PretixQuota q : quota) {
            var a = quotaFinder.getAvailability(event, q.getId());
            if (a.isPresent()) {
                PretixQuotaAvailability av = a.get();
                log.debug("Check if {} < {} ({} < {})",
                        av, availability,
                        av.calcEffectiveRemainig(), availability == null ? null : availability.calcEffectiveRemainig());
                if (availability == null || av.calcEffectiveRemainig() < availability.calcEffectiveRemainig()) {
                    availability = av;
                }
            }
        }
        return Optional.ofNullable(availability);
    }

    @Override
    public void resetCache() {
        log.info("[PRETIX] Resetting cache for pretix information");

        try {
            lock.writeLock().lock();
            invalidateEventsCache();
            invalidateEventStructCache();

            // reloading events
            reloadEvents();
            reloadEventStructure();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void invalidateEventsCache() {
        log.info("[PRETIX] Resetting event cache");
        currentEvent.set(null);
        otherEvents.set(null);
    }

    private void invalidateEventStructCache() {
        log.info("[PRETIX] Resetting event's struct cache");

        //General
        itemIdsCache.invalidateAll();
        itemIdToPrice.invalidateAll();
        itemIdToBundle.invalidateAll();
        itemIdToQuota.invalidateAll();

        //Questions
        questionUserId.set(-1L);
        questionUserNotes.set(-1L);
        questionDuplicateData.set(-1L);
        questionIdToType.invalidateAll();
        questionIdToIdentifier.invalidateAll();
        questionIdToOptions.invalidateAll();
        questionIdentifierToId.invalidateAll();

        //Tickets
        dailyIdToDay.invalidateAll();

        //Sponsors
        sponsorshipIdToType.invalidateAll();

        //Extra days
        extraDaysIdToDay.invalidateAll();

        //Rooms
        roomIdToInfo.invalidateAll();
        roomPretixItemIdToNames.invalidateAll();
        roomIdToEarlyExtraDayItemId.invalidateAll();
        roomIdToLateExtraDayItemId.invalidateAll();
        roomCapacityToItemId.invalidateAll();
    }


    private void reloadEvents() {
        var r = useCaseExecutor.execute(ReloadEventsUseCase.class, UseCaseInput.EMPTY);
        r.getLeft().ifPresent(event -> {
            log.info("[PRETIX] Setting an event as current = '{}'", event);
            currentEvent.set(event);
        });
        otherEvents.set(r.getRight());
    }

    private void loadCurrentEventFromDb() {
        String slug = PretixGenericUtils.buildOrgEventSlug(
                pretixConfig.getDefaultEvent(),
                pretixConfig.getDefaultOrganizer()
        );
        Event evt = eventFinder.findEventBySlug(slug);
        if (evt != null) {
            log.info("[PRETIX] Loaded current event from db: '{}'", evt);
            currentEvent.set(evt);
        } else {
            log.error("[PRETIX] Unable to load current event from db. CURRENT EVENT NOT SET!!");
        }
    }

    private void reloadEventStructure() {
        Consumer<Event> reload = e -> {
            reloadQuestions(e);
            reloadProducts(e);
            reloadQuotas(e);
        };
        reload.accept(getCurrentEvent());
        getOtherEvents().stream().filter(e -> !e.equals(getCurrentEvent())).forEach(reload);
    }

    private void reloadQuestions(@NotNull Event event) {
        List<PretixQuestion> questionList = useCaseExecutor.execute(ReloadQuestionsUseCase.class, event);
        questionList.forEach(question -> {
            long questionId = question.getId();
            QuestionType questionType = question.getType();
            List<PretixOption> options = question.getOptions();
            String questionIdentifier = question.getIdentifier();

            questionIdToType.put(questionId, questionType);
            questionIdToIdentifier.put(questionId, questionIdentifier);
            questionIdToOptions.put(questionId, options);
            questionIdentifierToId.put(questionIdentifier, questionId);
        });
        // searching QUESTIONS_ACCOUNT_USERID
        Long accountUserId = questionIdentifierToId.getIfPresent(QUESTIONS_ACCOUNT_USERID);
        if (accountUserId != null) {
            log.info("[PRETIX] Question account user id found, setup it on value = '{}'", accountUserId);
            questionUserId.set(accountUserId);
        } else {
            log.warn("[PRETIX] Question account user id not found");
        }
        Long duplicateData = questionIdentifierToId.getIfPresent(QUESTIONS_DUPLICATE_DATA);
        if (duplicateData != null) {
            log.info("[PRETIX] Question duplicate data found, setup it on value = '{}'", duplicateData);
            questionDuplicateData.set(duplicateData);
        } else {
            log.warn("[PRETIX] Question duplicate data not found");
        }
        Long userNotes = questionIdentifierToId.getIfPresent(QUESTIONS_USER_NOTES);
        if (userNotes != null) {
            log.info("[PRETIX] Question user  notes found, setup it on value = '{}'", userNotes);
            questionUserNotes.set(userNotes);
        } else {
            log.warn("[PRETIX] Question user notes not found");
        }
    }

    private void reloadProducts(@NotNull Event event) {
        PretixProductResults products = useCaseExecutor.execute(ReloadProductsUseCase.class, event);

        itemIdsCache.put(CacheItemTypes.TICKETS, products.ticketItemIds());
        itemIdsCache.put(CacheItemTypes.MEMBERSHIP_CARDS, products.membershipCardItemIds());
        itemIdsCache.put(CacheItemTypes.SPONSORSHIPS, products.sponsorshipItemIds());
        itemIdsCache.put(CacheItemTypes.ROOMS, products.roomItemIds());
        itemIdsCache.put(CacheItemTypes.NO_ROOM_ITEM, products.noRoomItemIds());
        itemIdsCache.put(CacheItemTypes.EXTRA_FURSUITS, products.extraFursuitsItemIds());
        itemIdsCache.put(CacheItemTypes.TEMP_ADDON, products.tempAddons());
        itemIdsCache.put(CacheItemTypes.TEMP_ITEM, products.tempItems());

        dailyIdToDay.putAll(products.dailyIdToDay());
        sponsorshipIdToType.putAll(products.sponsorshipIdToType());
        extraDaysIdToDay.putAll(products.extraDaysIdToDay());
        roomIdToInfo.putAll(products.roomIdToInfo());
        itemIdToPrice.putAll(products.itemIdToPrice());
        itemIdToBundle.putAll(products.itemIdToBundle());
        roomPretixItemIdToNames.putAll(products.roomPretixItemIdToNames());
        roomIdToEarlyExtraDayItemId.putAll(products.earlyDaysItemId());
        roomIdToLateExtraDayItemId.putAll(products.lateDaysItemId());
        roomCapacityToItemId.putAll(products.capacityToRoomItemIds());
    }

    private void reloadQuotas(@NotNull Event event) {
        List<PretixQuota> quotas = useCaseExecutor.execute(ReloadQuotaUseCase.class, event);
        quotas.forEach(quota -> quota.getItems().forEach(i -> {
            List<PretixQuota> existingQuota = itemIdToQuota.getIfPresent(i);
            //An item can be assigned to multiple quota
            if (existingQuota == null) {
                existingQuota = new LinkedList<>();
                itemIdToQuota.put(i, existingQuota);
            }
            existingQuota.add(quota);
        }));
    }

    @Override
    public void reloadAllOrders() {
        var input = new ReloadOrdersUseCase.Input(getCurrentEvent(), this);
        useCaseExecutor.execute(ReloadOrdersUseCase.class, input);
    }

    @Scheduled(cron = "${pretix.cache-reload-cronjob}")
    private void cronReloadCache() {
        log.info("[PRETIX] Cronjob running");
        reloadCacheAndOrders();
    }
}
