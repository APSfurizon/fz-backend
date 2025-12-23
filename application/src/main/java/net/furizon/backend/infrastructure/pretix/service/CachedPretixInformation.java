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
import net.furizon.backend.feature.pretix.objects.product.PretixProductVariation;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixVariationFinder;
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
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.apache.logging.log4j.util.TriConsumer;
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
    private final PretixProductFinder productFinder;
    @NotNull
    private final PretixVariationFinder variationFinder;
    @NotNull
    private final PretixHealthcheck healthcheck;
    @NotNull
    private final PretixConfig pretixConfig;
    @NotNull
    private final FursuitConfig fursuitConfig;

    // *** CACHE
    @NotNull
    private PretixCache currentEventCache = new PretixCache();
    @NotNull
    private PretixEventSpecificCache currentEventSpecificCache = new PretixEventSpecificCache();
    @NotNull
    private PretixCache otherEventsCache = new PretixCache();
    @NotNull
    private final Cache<Long, PretixEventSpecificCache> eventSpecificCache = Caffeine.newBuilder().build();

    @NotNull
    private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);
    @NotNull
    private final AtomicReference<List<Event>> otherEvents = new AtomicReference<>(null);

    //Countries
    @NotNull
    private final Cache<String, List<PretixState>> statesOfCountry = Caffeine.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .build();

    @Override
    public @Nullable Long getItemPrice(long itemId, boolean ignoreCache, boolean subtractBundlesPrice) {
        return getItemPrice(itemId, ignoreCache, subtractBundlesPrice, getCurrentEvent(), true);
    }
    @Override
    public @Nullable Long getItemPrice(long itemId, boolean ignoreCache, boolean subtractBundlesPrice,
                                       @Nullable Event event) {
        boolean isCurrentEvent = Objects.equals(event, getCurrentEvent());
        return getItemPrice(itemId, ignoreCache, subtractBundlesPrice, event, isCurrentEvent);
    }
    private @Nullable Long getItemPrice(long itemId, boolean ignoreCache, boolean subtractBundlesPrice,
                                       @Nullable Event event, boolean isCurrentEvent) {
        // We trust that Event and isCurrentEvent are set correctly
        PretixCache cache = isCurrentEvent ? currentEventCache : otherEventsCache;
        if (ignoreCache) {
            if (event == null) {
                log.error("[PRETIX] getItemPrice() fetch: Event is null");
                return null;
            }
            var v = productFinder.fetchProductById(getCurrentEvent(), itemId);
            if (!v.isPresent()) {
                log.error("[PRETIX] getItemPrice() fetch: Product not found for id {}", itemId);
                return null;
            }
            PretixProduct product = v.get();
            long value = product.getLongPrice();
            List<PretixProductBundle> bundles = product.getBundles();
            lock.writeLock().lock();
            cache.itemIdToPrice.put(itemId, value);
            cache.itemIdToBundle.put(itemId, bundles);
            lock.writeLock().unlock();
            if (subtractBundlesPrice) {
                value -= bundles.stream().mapToLong(PretixProductBundle::getTotalPrice).sum();
            }
            return value;
        } else {
            //When reloading the event we already cache the various prices, so there always be a value present
            lock.readLock().lock();
            Long value = cache.itemIdToPrice.getIfPresent(itemId);
            List<PretixProductBundle> bundles = cache.itemIdToBundle.getIfPresent(itemId);
            lock.readLock().unlock();
            if (bundles != null && subtractBundlesPrice) {
                value -= bundles.stream().mapToLong(PretixProductBundle::getTotalPrice).sum();
            }
            return value;
        }
    }

    @Override
    public @Nullable Long getVariationPrice(long variationId, boolean ignoreCache) {
        return getVariationPrice(variationId, ignoreCache, getCurrentEvent(), true);
    }
    @Override
    public @Nullable Long getVariationPrice(long variationId, boolean ignoreCache, @Nullable Event event) {
        boolean isCurrentEvent = Objects.equals(event, getCurrentEvent());
        return getVariationPrice(variationId, ignoreCache, event, isCurrentEvent);
    }
    private @Nullable Long getVariationPrice(long variationId, boolean ignoreCache,
                                             @Nullable Event event, boolean isCurrentEvent) {
        // We trust that Event and isCurrentEvent are set correctly
        PretixCache cache = isCurrentEvent ? currentEventCache : otherEventsCache;
        if (ignoreCache) {
            if (event == null) {
                log.error("[PRETIX] getVariationPrice() fetch: Event is null");
                return null;
            }
            lock.readLock().lock();
            Long itemId = cache.variationIdToItem.getIfPresent(variationId);
            lock.readLock().unlock();
            if (itemId == null) {
                log.error("[PRETIX] getVariationPrice(): Product not found for variation id {}", variationId);
                return null;
            }
            var v = variationFinder.fetchVariationById(event, itemId, variationId);
            if (!v.isPresent()) {
                log.error("[PRETIX] getVariationPrice() fetch: Product not found for item/variation {}/{}",
                          itemId, variationId);
                return null;
            }
            PretixProductVariation variation = v.get();
            long value = variation.getLongPrice();
            lock.writeLock().lock();
            cache.variationIdToPrice.put(variationId, value);
            lock.writeLock().unlock();
            return value;
        } else {
            //When reloading the event we already cache the various prices, so there always be a value present
            lock.readLock().lock();
            Long value = cache.variationIdToPrice.getIfPresent(variationId);
            lock.readLock().unlock();
            return value;
        }
    }

    @Override
    public @Nullable List<PretixProductBundle> getBundlesForItem(long itemId) {
        return getFromCachesById(itemId, currentEventCache.itemIdToBundle, otherEventsCache.itemIdToBundle);
    }

    @Override
    public @Nullable Long getFatherItemByVariationId(long variationId) {
        return getFromCachesById(variationId, currentEventCache.variationIdToItem, otherEventsCache.variationIdToItem);
    }

    @Override
    public @NotNull Set<Long> getCurrentEventRoomPretixIds() {
        lock.readLock().lock();
        Set<Long> v = new HashSet<Long>(currentEventCache.roomIdToInfo.asMap().keySet());
        lock.readLock().unlock();
        return v;
    }

    @Override
    public @Nullable HotelCapacityPair getRoomInfoFromPretixItemId(long roomPretixItemId) {
        return getFromCachesById(roomPretixItemId, currentEventCache.roomIdToInfo, otherEventsCache.roomIdToInfo);
    }

    @Override
    public @NotNull List<Long> getRoomItemIdsForCapacity(short capacity) {
        return getRoomItemIdsForCapacity(capacity, currentEventSpecificCache);
    }
    @Override
    public @NotNull List<Long> getRoomItemIdsForCapacity(short capacity, long eventId) {
        return getRoomItemIdsForCapacity(capacity, getSpecificCache(eventId));
    }
    private @NotNull List<Long> getRoomItemIdsForCapacity(short capacity, @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        List<Long> v = cache.roomCapacityToItemId.getIfPresent(capacity);
        lock.readLock().unlock();
        return v == null ? Collections.emptyList() : v;
    }

    @Override
    public @Nullable HotelCapacityPair getBiggestRoomCapacityOnCurrentEvent() {
        HotelCapacityPair biggest = null;
        short maxCapacity = 0;
        lock.readLock().lock();
        for (HotelCapacityPair p : currentEventCache.roomIdToInfo.asMap().values()) {
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
    public Map<String, String> getItemNames(long itemId) {
        var v = getFromCachesById(itemId, currentEventCache.itemIdToNames, otherEventsCache.itemIdToNames);
        if (v == null) {
            log.warn("Unable to fetch item name for id {}", itemId);
        }
        return v == null ? new HashMap<>() : v;
    }

    @NotNull
    @Override
    public Map<String, String> getVariationNames(long variationId) {
        var v = getFromCachesById(variationId,
                currentEventCache.variationIdToNames, otherEventsCache.variationIdToNames);
        if (v == null) {
            log.warn("Unable to fetch variation name for id {}", variationId);
        }
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
    public Long getExtraDayItemIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                            short capacity, @NotNull ExtraDays day, long eventId) {
        return getExtraDayItemIdForHotelCapacity(
                new HotelCapacityPair(hotelName, roomInternalName, capacity),
                day, eventId
        );
    }
    @Nullable
    @Override
    public Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day) {
        return getExtraDayItemIdForHotelCapacity(pair, day, currentEventSpecificCache);
    }
    @Nullable
    @Override
    public Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day,
                                                  long eventId) {
        return getExtraDayItemIdForHotelCapacity(pair, day, getSpecificCache(eventId));
    }
    @Nullable
    private Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day,
                                                   @NotNull PretixEventSpecificCache cache) {
        Cache<HotelCapacityPair, Long> map = null;
        if (day == ExtraDays.EARLY) {
            map = cache.roomIdToEarlyExtraDayItemId;
        } else if (day == ExtraDays.LATE) {
            map = cache.roomIdToLateExtraDayItemId;
        }
        if (map == null) {
            return null;
        }
        lock.readLock().lock();
        Long ret = map.getIfPresent(pair);
        lock.readLock().unlock();
        return ret;
    }

    @Nullable
    @Override
    public Long getBoardVariationIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                    short capacity, @NotNull Board board) {
        return getBoardVariationIdForHotelCapacity(new HotelCapacityPair(hotelName, roomInternalName, capacity), board);
    }
    @Nullable
    @Override
    public Long getBoardVariationIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                              short capacity, @NotNull Board board, long eventId) {
        return getBoardVariationIdForHotelCapacity(
                new HotelCapacityPair(hotelName, roomInternalName, capacity),
                board, eventId
        );
    }
    @Nullable
    @Override
    public Long getBoardVariationIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull Board board) {
        return getBoardVariationIdForHotelCapacity(pair, board, currentEventSpecificCache);
    }
    @Nullable
    @Override
    public Long getBoardVariationIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull Board board,
                                                    long eventId) {
        return getBoardVariationIdForHotelCapacity(pair, board, getSpecificCache(eventId));
    }
    @Nullable
    private Long getBoardVariationIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull Board board,
                                                     @NotNull PretixEventSpecificCache cache) {
        Cache<HotelCapacityPair, Long> map = null;
        if (board == Board.HALF) {
            map = cache.halfBoardCapacityToVariationId;
        } else if (board == Board.FULL) {
            map = cache.fullBoardCapacityToVariationId;
        }
        if (map == null) {
            return null;
        }
        lock.readLock().lock();
        Long ret = map.getIfPresent(pair);
        lock.readLock().unlock();
        return ret;
    }
    @Nullable
    @Override
    public Long getBoardItemIdForHotelCapacity(@NotNull HotelCapacityPair pair) {
        return getBoardItemIdForHotelCapacity(pair, currentEventSpecificCache);
    }
    @Nullable
    @Override
    public Long getBoardItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, long eventId) {
        return getBoardItemIdForHotelCapacity(pair, getSpecificCache(eventId));
    }
    @Nullable
    private Long getBoardItemIdForHotelCapacity(@NotNull HotelCapacityPair pair,
                                                @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        Long ret = cache.boardCapacityToItemId.getIfPresent(pair);
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
        return getIdsForItemType(type, currentEventSpecificCache);
    }
    @NotNull
    @Override
    public Set<Long> getIdsForItemType(@NotNull CacheItemTypes type, long eventId) {
        return getIdsForItemType(type, getSpecificCache(eventId));
    }
    @NotNull
    private Set<Long> getIdsForItemType(@NotNull CacheItemTypes type, @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.itemIdsCache.getIfPresent(type);
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
        return getQuestionUserId(currentEventSpecificCache);
    }
    @Override
    public long getQuestionUserId(long eventId) {
        return getQuestionUserId(getSpecificCache(eventId));
    }
    private long getQuestionUserId(@NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.questionUserId.get();
        lock.readLock().unlock();
        return v;
    }

    @Override
    public long getQuestionDuplicateData() {
        return getQuestionDuplicateData(currentEventSpecificCache);
    }
    @Override
    public long getQuestionDuplicateData(long eventId) {
        return getQuestionDuplicateData(getSpecificCache(eventId));
    }
    private long getQuestionDuplicateData(@NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.questionDuplicateData.get();
        lock.readLock().unlock();
        return v;
    }

    @Override
    public long getQuestionUserNotes() {
        return getQuestionUserNotes(currentEventSpecificCache);
    }
    @Override
    public long getQuestionUserNotes(long eventId) {
        return getQuestionUserNotes(getSpecificCache(eventId));
    }
    private long getQuestionUserNotes(@NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.questionUserNotes.get();
        lock.readLock().unlock();
        return v;
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromId(long id) {
        return Optional.ofNullable(
                getFromCachesById(id, currentEventCache.questionIdToType, otherEventsCache.questionIdToType)
        );
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier) {
        return getQuestionTypeFromIdentifier(identifier, currentEventSpecificCache);
    }
    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier, long eventId) {
        return getQuestionTypeFromIdentifier(identifier, getSpecificCache(eventId));
    }
    @NotNull
    private Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier,
                                                                 @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        Long qid = cache.questionIdentifierToId.getIfPresent(identifier);
        if (qid == null) {
            return Optional.empty();
        }
        QuestionType v = getFromCachesById(qid, currentEventCache.questionIdToType, otherEventsCache.questionIdToType);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<String> getQuestionIdentifierFromId(long id) {
        return Optional.ofNullable(
                getFromCachesById(id, currentEventCache.questionIdToIdentifier, otherEventsCache.questionIdToIdentifier)
        );
    }

    @Override
    public @NotNull Optional<List<PretixOption>> getQuestionOptionsFromId(long id) {
        return Optional.ofNullable(
                getFromCachesById(id, currentEventCache.questionIdToOptions, otherEventsCache.questionIdToOptions)
        );
    }

    @NotNull
    @Override
    public Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier) {
        return getQuestionIdFromIdentifier(identifier, currentEventSpecificCache);
    }
    @NotNull
    @Override
    public Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier, long eventId) {
        return getQuestionIdFromIdentifier(identifier, getSpecificCache(eventId));
    }
    @NotNull
    private Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier,
                                                       @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.questionIdentifierToId.getIfPresent(identifier);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Set<Long> getSponsorIds(@NotNull Sponsorship sponsorship) {
        return getSponsorIds(sponsorship, currentEventSpecificCache);
    }
    @NotNull
    @Override
    public Set<Long> getSponsorIds(@NotNull Sponsorship sponsorship, long eventId) {
        return getSponsorIds(sponsorship, getSpecificCache(eventId));
    }
    @NotNull
    private Set<Long> getSponsorIds(@NotNull Sponsorship sponsorship, @NotNull PretixEventSpecificCache cache) {
        lock.readLock().lock();
        var v = cache.sponsorshipTypeToIds.getIfPresent(sponsorship);
        lock.readLock().unlock();
        return v == null ? Collections.emptySet() : v;
    }

    @NotNull
    @Override
    public Optional<Order> parseOrder(@NotNull PretixOrder pretixOrder, @NotNull Event event) {
        lock.readLock().lock();
        try {
            boolean isCurrentEvent = event.equals(getCurrentEvent());
            PretixCache mainCache = isCurrentEvent
                                  ? currentEventCache
                                  : otherEventsCache;
            PretixEventSpecificCache specificCache = isCurrentEvent
                                                   ? currentEventSpecificCache
                                                   : eventSpecificCache.getIfPresent(event.getId());
            if (mainCache == null || specificCache == null) {
                log.error("Trying to parse an order ({}) for an unloaded event {}",
                          pretixOrder.getCode(), event.getId());
                return Optional.empty();
            }

            Integer cacheDay;
            ExtraDays cacheExtraDays;
            BiFunction<CacheItemTypes, @NotNull Long, @NotNull Boolean> checkItemId = (type, item) ->
                    Objects.requireNonNull(specificCache.itemIdsCache.getIfPresent(type)).contains(item);

            long questionUserId = specificCache.questionUserId.get();
            long questionDuplicateData = specificCache.questionDuplicateData.get();

            boolean hasTicket = false; //If no ticket is found, we don't store the order at all
            boolean foundDuplicate = false;
            OrderStatus status = OrderStatus.get(pretixOrder.getStatus());

            Set<Integer> days = new HashSet<>();
            Sponsorship sponsorship = Sponsorship.NONE;
            ExtraDays extraDays = ExtraDays.NONE;
            Board board =  Board.NONE;
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
            Long boardPositionId = null;
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
                        QuestionType questionType = mainCache.questionIdToType.getIfPresent(questionId);
                        if (questionType != null) {
                            if (questionType == QuestionType.FILE) {
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

                } else if ((cacheDay = mainCache.dailyIdToDay.getIfPresent(itemId)) != null) {
                    days.add(cacheDay);

                } else if (checkItemId.apply(CacheItemTypes.MEMBERSHIP_CARDS, itemId)) {
                    membership = true;

                } else if (checkItemId.apply(CacheItemTypes.SPONSORSHIPS, itemId)) {
                    Sponsorship s = mainCache.sponsorshipIdToType.getIfPresent(position.getVariationId());
                    if (s != null && s.ordinal() > sponsorship.ordinal()) {
                        sponsorship = s; //keep the best sponsorship
                    }

                } else if ((cacheExtraDays = mainCache.extraDaysIdToDay.getIfPresent(itemId)) != null) {
                    if (cacheExtraDays == ExtraDays.EARLY) {
                        earlyPositionId = position.getPositionId();
                    } else if (cacheExtraDays == ExtraDays.LATE) {
                        latePositionId = position.getPositionId();
                    }
                    extraDays = ExtraDays.or(extraDays, cacheExtraDays);

                } else if (checkItemId.apply(CacheItemTypes.BOARDS, itemId)) {
                    Board b = mainCache.variationIdToBoard.getIfPresent(position.getVariationId());
                    if (b != null && b.ordinal() > board.ordinal()) {
                        //Keep the best board
                        boardPositionId = position.getPositionId();
                        board = b;
                    }


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
                        HotelCapacityPair room = mainCache.roomIdToInfo.getIfPresent(itemId);
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
                    .board(board)
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
                    .boardPositionId(boardPositionId)
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
        return getFromCachesById(itemId,
                currentEventCache.itemIdToQuota, otherEventsCache.itemIdToQuota);
    }

    @Override
    public @Nullable List<PretixQuota> getQuotaFromVariationId(long variationId) {
        return getFromCachesById(variationId,
                currentEventCache.variationIdToQuota, otherEventsCache.variationIdToQuota);
    }

    @NotNull
    private PretixEventSpecificCache getSpecificCache(long eventId) {
        var cache = getCurrentEvent().getId() == eventId
                  ? currentEventSpecificCache
                  : eventSpecificCache.getIfPresent(eventId);
        if (cache == null) {
            log.error("[PRETIX] getSpecificCache({}): Cache is null! Returning current event cache", eventId);
            cache = currentEventSpecificCache;
        }
        return cache;
    }

    @Nullable
    @SafeVarargs
    private <R> R getFromCachesById(long id, Cache<Long, R>... caches) {
        R result = null;
        lock.readLock().lock();
        for (Cache<Long, R> cache : caches) {
            result = cache.getIfPresent(id);
            if (result != null) {
                break;
            }
        }
        lock.readLock().unlock();
        return result;
    }

    @Override
    public @NotNull Optional<PretixQuotaAvailability> getSmallestAvailabilityFromItemId(long itemId) {
        return getSmallestAvailabilityFromList(itemId, getQuotaFromItemId(itemId));
    }
    @Override
    public @NotNull Optional<PretixQuotaAvailability> getSmallestAvailabilityFromVariationId(long variationId) {
        return getSmallestAvailabilityFromList(variationId, getQuotaFromVariationId(variationId));
    }
    //This is NOT cached!!
    @NotNull
    private Optional<PretixQuotaAvailability> getSmallestAvailabilityFromList(long id,
                                                                              @Nullable List<PretixQuota> quota) {
        if (quota == null || quota.isEmpty()) {
            log.warn("Quota not found for item/variation {}", id);
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
            log.debug("Quota {}: Items={} Variations={}", q.getId(), q.getItems(), q.getVariations());
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
    public void reloadCacheAndOrders(boolean reloadPastEvents) {
        if (!pretixConfig.isEnableSync()) {
            log.warn("[PRETIX] Pretix synchronization has been disabled");
            loadCurrentEventFromDb();
            return;
        }
        healthcheck.waitForPretix();
        log.info("[PRETIX] Syncing pretix information and cache it");
        long start = System.currentTimeMillis();
        resetEventStructCache(reloadPastEvents);
        reloadCurrentEventOrders();
        log.info("[PRETIX] Reloading cache and orders required {} ms", System.currentTimeMillis() - start);
    }

    @Override
    public void resetEventStructCache(boolean reloadPastEvents) {
        log.info("[PRETIX] Resetting cache for event's pretix information");

        try {
            lock.writeLock().lock();
            invalidateEventsCache();
            invalidateEventStructCache(reloadPastEvents);

            // reloading events
            reloadEvents();
            reloadEventStructure(reloadPastEvents);
        } finally {
            lock.writeLock().unlock();
        }
    }

    //Events cache is always loaded together, so we must invalidate both as well
    private void invalidateEventsCache() {
        currentEvent.set(null);
        otherEvents.set(null);
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

    private void invalidateEventStructCache(boolean pastEvents) {
        log.info("[PRETIX] Resetting event's struct cache");
        currentEventCache.invalidate();
        currentEventSpecificCache.invalidate();
        if (pastEvents) {
            otherEventsCache.invalidate();
            eventSpecificCache.asMap().values().forEach(PretixEventSpecificCache::invalidate);
            eventSpecificCache.invalidateAll();
        }
    }

    private void reloadEventStructure(boolean reloadPastEvents) {
        TriConsumer<Event, PretixCache, PretixEventSpecificCache> reload = (e, cache, specificCache) -> {
            log.debug("[PRETIX] Reloading event {}", e);
            reloadQuestions(e, cache, specificCache);
            reloadProducts(e, cache, specificCache);
            reloadQuotas(e, cache);
        };
        Event current = getCurrentEvent();
        reload.accept(current, currentEventCache, currentEventSpecificCache);
        if (reloadPastEvents) {
            PretixCache cache = new PretixCache();
            getOtherEvents()
                .stream()
                .filter(e -> !e.equals(current) && !pretixConfig.isEventExcludedFromCache(e))
                .forEach(e -> {
                    long eventId = e.getId();
                    PretixEventSpecificCache specificCache = new PretixEventSpecificCache();
                    cache.invalidate();
                    try {
                        reload.accept(e, cache, specificCache);
                        eventSpecificCache.put(eventId, specificCache);
                        otherEventsCache.importCache(cache);
                    } catch  (Exception ex) {
                        log.warn("[PRETIX] Exception while loading event {}. Exception details:", e, ex);
                    }
                });
        }
    }

    private void reloadQuestions(@NotNull Event event,
                                 @NotNull PretixCache mainCache,
                                 @NotNull PretixEventSpecificCache eventSpecificCache) {
        List<PretixQuestion> questionList = useCaseExecutor.execute(ReloadQuestionsUseCase.class, event);
        var questionIdentifierToId = eventSpecificCache.questionIdentifierToId;
        questionList.forEach(question -> {
            long questionId = question.getId();
            QuestionType questionType = question.getType();
            List<PretixOption> options = question.getOptions();
            String questionIdentifier = question.getIdentifier();

            mainCache.questionIdToType.put(questionId, questionType);
            mainCache.questionIdToIdentifier.put(questionId, questionIdentifier);
            mainCache.questionIdToOptions.put(questionId, options);
            questionIdentifierToId.put(questionIdentifier, questionId);
        });
        // searching QUESTIONS_ACCOUNT_USERID
        Long accountUserId = questionIdentifierToId.getIfPresent(QUESTIONS_ACCOUNT_USERID);
        if (accountUserId != null) {
            log.info("[PRETIX] Question account user id found, setup it on value = '{}'", accountUserId);
            eventSpecificCache.questionUserId.set(accountUserId);
        } else {
            log.warn("[PRETIX] Question account user id not found");
        }
        Long duplicateData = questionIdentifierToId.getIfPresent(QUESTIONS_DUPLICATE_DATA);
        if (duplicateData != null) {
            log.info("[PRETIX] Question duplicate data found, setup it on value = '{}'", duplicateData);
            eventSpecificCache.questionDuplicateData.set(duplicateData);
        } else {
            log.warn("[PRETIX] Question duplicate data not found");
        }
        Long userNotes = questionIdentifierToId.getIfPresent(QUESTIONS_USER_NOTES);
        if (userNotes != null) {
            log.info("[PRETIX] Question user  notes found, setup it on value = '{}'", userNotes);
            eventSpecificCache.questionUserNotes.set(userNotes);
        } else {
            log.warn("[PRETIX] Question user notes not found");
        }
    }

    private void reloadProducts(@NotNull Event event,
                                @NotNull PretixCache mainCache,
                                @NotNull PretixEventSpecificCache eventSpecificCache) {
        PretixProductResults products = useCaseExecutor.execute(ReloadProductsUseCase.class, event);

        var itemIdsCache = eventSpecificCache.itemIdsCache;
        itemIdsCache.put(CacheItemTypes.TICKETS, products.ticketItemIds());
        itemIdsCache.put(CacheItemTypes.MEMBERSHIP_CARDS, products.membershipCardItemIds());
        itemIdsCache.put(CacheItemTypes.SPONSORSHIPS, products.sponsorshipItemIds());
        itemIdsCache.put(CacheItemTypes.ROOMS, products.roomItemIds());
        itemIdsCache.put(CacheItemTypes.NO_ROOM_ITEM, products.noRoomItemIds());
        itemIdsCache.put(CacheItemTypes.EXTRA_FURSUITS, products.extraFursuitsItemIds());
        itemIdsCache.put(CacheItemTypes.TEMP_ADDON, products.tempAddons());
        itemIdsCache.put(CacheItemTypes.TEMP_ITEM, products.tempItems());
        itemIdsCache.put(CacheItemTypes.BOARDS, products.boardItemIds());

        mainCache.dailyIdToDay.putAll(products.dailyIdToDay());
        mainCache.sponsorshipIdToType.putAll(products.sponsorshipIdToType());
        mainCache.extraDaysIdToDay.putAll(products.extraDaysIdToDay());
        mainCache.roomIdToInfo.putAll(products.roomIdToInfo());
        mainCache.itemIdToPrice.putAll(products.itemIdToPrice());
        mainCache.variationIdToPrice.putAll(products.variationIdToPrice());
        mainCache.itemIdToBundle.putAll(products.itemIdToBundle());
        mainCache.variationIdToItem.putAll(products.variationIdToFatherItemId());
        mainCache.itemIdToNames.putAll(products.itemIdToNames());
        mainCache.variationIdToNames.putAll(products.variationIdToNames());
        mainCache.variationIdToBoard.putAll(products.boardVariationIdToType());

        eventSpecificCache.sponsorshipTypeToIds.putAll(products.sponsorshipTypeToIds());
        eventSpecificCache.roomIdToEarlyExtraDayItemId.putAll(products.earlyDaysItemId());
        eventSpecificCache.roomIdToLateExtraDayItemId.putAll(products.lateDaysItemId());
        eventSpecificCache.roomCapacityToItemId.putAll(products.capacityToRoomItemIds());
        eventSpecificCache.boardCapacityToItemId.putAll(products.boardCapacityToItemId());
        eventSpecificCache.halfBoardCapacityToVariationId.putAll(products.halfBoardCapacityToVariationId());
        eventSpecificCache.fullBoardCapacityToVariationId.putAll(products.fullBoardCapacityToVariationId());
    }

    private void reloadQuotas(@NotNull Event event,
                              @NotNull PretixCache mainCache) {
        List<PretixQuota> quotas = useCaseExecutor.execute(ReloadQuotaUseCase.class, event);
        TriConsumer<PretixQuota, Long, Cache<Long, List<PretixQuota>>> store = (quota, id, cache) -> {
            List<PretixQuota> existingQuota = cache.getIfPresent(id);
            //An item can be assigned to multiple quota
            if (existingQuota == null) {
                existingQuota = new LinkedList<>();
                cache.put(id, existingQuota);
            }
            existingQuota.add(quota);
        };
        var itemIdToQuota = mainCache.itemIdToQuota;
        var variationIdToQuota = mainCache.variationIdToQuota;
        quotas.forEach(quota -> {
            quota.getItems().forEach(id -> store.accept(quota, id, itemIdToQuota));
            quota.getVariations().forEach(id -> store.accept(quota, id, variationIdToQuota));
        });
    }

    @Override
    public void reloadCurrentEventOrders() {
        var input = new ReloadOrdersUseCase.Input(getCurrentEvent(), this);
        useCaseExecutor.execute(ReloadOrdersUseCase.class, input);
    }

    @PostConstruct
    private void startup() {
        reloadCacheAndOrders(true);
    }

    @Scheduled(cron = "${pretix.cache-reload-cronjob}")
    private void cronReloadCache() {
        log.info("[PRETIX] Cronjob running");
        reloadCacheAndOrders(false);
    }
}
