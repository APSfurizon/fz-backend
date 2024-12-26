package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.usecase.ReloadOrdersUseCase;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.feature.pretix.objects.product.usecase.ReloadProductsUseCase;
import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.feature.pretix.objects.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.pretix.objects.quota.finder.PretixQuotaFinder;
import net.furizon.backend.feature.pretix.objects.quota.usecase.ReloadQuotaUseCase;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.feature.pretix.objects.states.usecase.FetchStatesByCountry;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.Const;
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

import static net.furizon.backend.infrastructure.pretix.Const.QUESTIONS_ACCOUNT_USERID;

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
    private final PretixConfig pretixConfig;

    // *** CACHE
    @NotNull
    private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);

    //Event Struct

    //Contains tickets, memberships, sponsors, rooms
    @NotNull
    private final Cache<CacheItemTypes, Set<Long>> itemIdsCache = Caffeine.newBuilder().build();

    //Questions
    @NotNull
    private final AtomicReference<Long> questionUserId = new AtomicReference<>(-1L);
    @NotNull
    private final Cache<Long, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<Long, String> questionIdToIdentifier = Caffeine.newBuilder().build();
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
    //map id -> price * 100
    @NotNull
    private final Cache<Long, Long> roomIdToPrice = Caffeine.newBuilder().build();
    //map capacity/name -> room name
    @NotNull
    private final Cache<Long, Map<String, String>> roomPretixItemIdToNames =
        Caffeine.newBuilder().build();


    //Quotas
    private final Cache<Long, List<PretixQuota>> itemIdToQuota = Caffeine.newBuilder().build();

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
            return;
        }
        log.info("[PRETIX] Syncing pretix information and cache it");
        long start = System.currentTimeMillis();
        resetCache();
        reloadAllOrders();
        log.info("[PRETIX] Reloading cache and orders required {} ms", System.currentTimeMillis() - start);
    }

    @NotNull
    @Override
    public Map<String, String> getRoomNamesFromRoomPretixItemId(long roomPretixItemId) {
        lock.readLock().lock();
        var v = roomPretixItemIdToNames.getIfPresent(roomPretixItemId);
        lock.readLock().unlock();
        return v == null ? new HashMap<>() : v;
    }

    @Override
    public @Nullable Long getRoomPriceByItemId(long roomPretixItemId, boolean ignoreCache) {
        if (ignoreCache) {
            var v = pretixProductFinder.fetchProductById(getCurrentEvent(), roomPretixItemId);
            if (!v.isPresent()) {
                return null;
            }
            long value = PretixGenericUtils.fromStrPriceToLong(v.get().getPrice());
            lock.writeLock().lock();
            roomIdToPrice.put(roomPretixItemId, value);
            lock.writeLock().unlock();
            return value;
        } else {
            lock.readLock().lock();
            Long value = roomIdToPrice.getIfPresent(roomPretixItemId);
            lock.readLock().unlock();
            return value;
        }
    }

    @Override
    public @NotNull Set<Long> getRoomPretixIds() {
        lock.readLock().lock();
        Set<Long> v = new HashSet<Long>(roomIdToPrice.asMap().keySet());
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

    @NotNull
    @Override
    public List<PretixState> getStatesOfCountry(String countryIsoCode) {
        //No cache lock needed!
        var ret = statesOfCountry.get(countryIsoCode, k -> useCaseExecutor.execute(FetchStatesByCountry.class, k));
        return ret != null ? ret : new LinkedList<>();
    }

    @NotNull
    @Override
    public Set<Long> getIdsForItemType(CacheItemTypes type) {
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

    public long getQuestionUserId() {
        lock.readLock().lock();
        var v = questionUserId.get();
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

            boolean hasTicket = false; //If no ticket is found, we don't store the order at all
            OrderStatus status = OrderStatus.get(pretixOrder.getStatus());

            Set<Integer> days = new HashSet<>();
            Sponsorship sponsorship = Sponsorship.NONE;
            ExtraDays extraDays = ExtraDays.NONE;
            List<PretixAnswer> answers = null;
            String hotelInternalName = null;
            Long pretixRoomItemId = null;
            long ticketPositionId = 0L;
            Long roomPositionId = null;
            boolean membership = false;
            short roomCapacity = 0;
            Long userId = null;

            List<PretixPosition> positions = pretixOrder.getPositions();
            if (positions.isEmpty()) {
                status = OrderStatus.CANCELED;
            }

            for (PretixPosition position : positions) {
                if (position.isCanceled()) {
                    continue;
                }

                long itemId = position.getItemId();

                if (checkItemId.apply(CacheItemTypes.TICKETS, itemId)) {
                    hasTicket = true;
                    ticketPositionId = position.getPositionId();
                    answers = position.getAnswers();
                    for (PretixAnswer answer : answers) {
                        long questionId = answer.getQuestionId();
                        var questionType = getQuestionTypeFromId(questionId);
                        if (questionType.isPresent()) {
                            if (questionType.get() == QuestionType.FILE) {
                                answer.setAnswer(Const.QUESTIONS_FILE_KEEP);
                            }
                            if (questionId == this.getQuestionUserId()) {
                                String s = answer.getAnswer();
                                if (s != null && !s.isBlank()) {
                                    userId = Long.parseLong(s);
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
                    if (extraDays != ExtraDays.BOTH) {
                        if (extraDays != cacheExtraDays && extraDays != ExtraDays.NONE) {
                            extraDays = ExtraDays.BOTH;
                        } else {
                            extraDays = cacheExtraDays;
                        }
                    }

                } else if (checkItemId.apply(CacheItemTypes.ROOMS, itemId)) {
                    //Set the room position and item id anyway, even if we have a NO_ROOM item
                    roomPositionId = position.getPositionId();
                    pretixRoomItemId = itemId;
                    if (checkItemId.apply(CacheItemTypes.NO_ROOM_ITEM, itemId)) {
                        roomCapacity = 0;
                        hotelInternalName = null;
                    } else {
                        HotelCapacityPair room = roomIdToInfo.getIfPresent(itemId);
                        if (room != null) {
                            roomCapacity = room.capacity();
                            hotelInternalName = room.hotelInternalName();
                        }
                    }
                }
            }

            if (!hasTicket) {
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
                    .pretixOrderSecret(pretixOrder.getSecret())
                    .hasMembership(membership)
                    .ticketPositionId(ticketPositionId)
                    .roomPositionId(roomPositionId)
                    .eventId(event.getId())
                    .orderOwnerUserId(userId)
                    .answers(answers, this)
                    .userFinder(userFinder)
                    .eventFinder(eventFinder)
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

        lock.writeLock().lock();
        try {
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
    }

    private void invalidateEventStructCache() {
        log.info("[PRETIX] Resetting event's struct cache");

        //General
        itemIdsCache.invalidateAll();

        //Questions
        questionUserId.set(-1L);
        questionIdToType.invalidateAll();
        questionIdToIdentifier.invalidateAll();
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
        roomIdToPrice.invalidateAll();

        //Quotas
        itemIdToQuota.invalidateAll();
    }


    private void reloadEvents() {
        useCaseExecutor
            .execute(ReloadEventsUseCase.class, UseCaseInput.EMPTY)
            .ifPresent(event -> {
                log.info("[PRETIX] Setting an event as current = '{}'", event);
                currentEvent.set(event);
            });
    }

    private void reloadEventStructure() {
        Event event = getCurrentEvent();
        reloadQuestions(event);
        reloadProducts(event);
        reloadQuotas(event);
    }

    private void reloadQuestions(@NotNull Event event) {
        List<PretixQuestion> questionList = useCaseExecutor.execute(ReloadQuestionsUseCase.class, event);
        questionList.forEach(question -> {
            long questionId = question.getId();
            QuestionType questionType = question.getType();
            String questionIdentifier = question.getIdentifier();

            questionIdToType.put(questionId, questionType);
            questionIdToIdentifier.put(questionId, questionIdentifier);
            questionIdentifierToId.put(questionIdentifier, questionId);
        });
        // searching QUESTIONS_ACCOUNT_USERID
        Long accountUserId = questionIdentifierToId.getIfPresent(QUESTIONS_ACCOUNT_USERID);
        if (accountUserId != null) {
            log.info("[PRETIX] Question account user id found, setup it on value = '{}'", accountUserId);
            questionUserId.set(accountUserId);
        }
    }

    private void reloadProducts(@NotNull Event event) {
        PretixProductResults products = useCaseExecutor.execute(ReloadProductsUseCase.class, event);

        itemIdsCache.put(CacheItemTypes.TICKETS, products.ticketItemIds());
        itemIdsCache.put(CacheItemTypes.MEMBERSHIP_CARDS, products.membershipCardItemIds());
        itemIdsCache.put(CacheItemTypes.SPONSORSHIPS, products.sponsorshipItemIds());
        itemIdsCache.put(CacheItemTypes.ROOMS, products.roomItemIds());
        itemIdsCache.put(CacheItemTypes.NO_ROOM_ITEM, products.noRoomItemIds());

        dailyIdToDay.putAll(products.dailyIdToDay());
        sponsorshipIdToType.putAll(products.sponsorshipIdToType());
        extraDaysIdToDay.putAll(products.extraDaysIdToDay());
        roomIdToInfo.putAll(products.roomIdToInfo());
        roomIdToPrice.putAll(products.roomIdToPrice());
        roomPretixItemIdToNames.putAll(products.roomPretixItemIdToNames());
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
