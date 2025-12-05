package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProductBundle;
import net.furizon.backend.feature.pretix.objects.question.PretixOption;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PretixInformation {

    void reloadCacheAndOrders(boolean reloadPastEvents);

    @NotNull Set<Long> getIdsForItemType(@NotNull CacheItemTypes type, long eventId);

    @NotNull Set<Long> getIdsForItemType(@NotNull CacheItemTypes type);


    @NotNull
    Event getCurrentEvent();

    @Nullable
    Long getItemPrice(long roomPretixItemId, boolean ignoreCache, boolean subtractBundlesPrice);

    @Nullable Long getItemPrice(long itemId, boolean ignoreCache, boolean subtractBundlesPrice, @Nullable Event event);

    @Nullable
    Long getVariationPrice(long variationId, boolean ignoreCache);

    @Nullable Long getVariationPrice(long variationId, boolean ignoreCache, @Nullable Event event);

    @Nullable List<PretixProductBundle> getBundlesForItem(long itemId);

    @Nullable Long getFatherItemByVariationId(long variationId);

    @NotNull
    Set<Long> getCurrentEventRoomPretixIds();
    @NotNull
    Map<String, String> getItemNames(long itemId);
    @Nullable
    HotelCapacityPair getRoomInfoFromPretixItemId(long roomPretixItemId);

    @NotNull List<Long> getRoomItemIdsForCapacity(short capacity);

    @NotNull List<Long> getRoomItemIdsForCapacity(short capacity, long eventId);

    @Nullable
    HotelCapacityPair getBiggestRoomCapacityOnCurrentEvent();

    @NotNull Map<String, String> getVariationNames(long variationId);

    @Nullable Long getExtraDayItemIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                           short capacity, @NotNull ExtraDays day);

    @Nullable Long getExtraDayItemIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                     short capacity, @NotNull ExtraDays day, long eventId);

    @Nullable Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day);

    @Nullable Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day,
                                                     long eventId);

    @Nullable Long getBoardVariationIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                       short capacity, @NotNull Board board);

    @Nullable Long getBoardVariationIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                                       short capacity, @NotNull Board board, long eventId);

    @Nullable Long getBoardVariationIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull Board board);

    @Nullable Long getBoardVariationIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull Board board,
                                                       long eventId);

    @Nullable Long getBoardItemIdForHotelCapacity(@NotNull HotelCapacityPair pair);

    @Nullable Long getBoardItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, long eventId);

    @NotNull
    List<PretixState> getStatesOfCountry(@NotNull String countryIsoCode);

    boolean isCountryValid(@NotNull String countryIsoCode);

    boolean isRegionOfCountryValid(@NotNull String countryIsoCode, @NotNull String region);

    boolean isPhonePrefixValid(@NotNull String phonePrefix);

    long getQuestionUserId();

    long getQuestionUserId(long eventId);

    long getQuestionDuplicateData();

    long getQuestionDuplicateData(long eventId);

    long getQuestionUserNotes();

    long getQuestionUserNotes(long eventId);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromId(long id);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier);

    @NotNull Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier, long eventId);

    @NotNull
    Optional<String> getQuestionIdentifierFromId(long id);

    @NotNull Optional<List<PretixOption>> getQuestionOptionsFromId(long id);

    @NotNull
    Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier);

    @NotNull Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier, long eventId);

    @NotNull Set<Long> getSponsorIds(@NotNull Sponsorship sponsorship);

    @NotNull Set<Long> getSponsorIds(@NotNull Sponsorship sponsorship, long eventId);

    @NotNull
    Optional<Order> parseOrder(@NotNull PretixOrder pretixOrder, @NotNull Event event);

    @Nullable List<PretixQuota> getQuotaFromItemId(long itemId);

    @Nullable List<PretixQuota> getQuotaFromVariationId(long variationId);

    @NotNull
    Optional<PretixQuotaAvailability> getSmallestAvailabilityFromItemId(long itemId);

    @NotNull Optional<PretixQuotaAvailability> getSmallestAvailabilityFromVariationId(long variationId);

    void resetEventStructCache(boolean reloadPastEvents);

    void reloadCurrentEventOrders();
}
