package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PretixInformation {

    void reloadCacheAndOrders();

    @NotNull
    Event getCurrentEvent();

    long getQuestionUserId();

    @Nullable
    Long getItemPrice(long roomPretixItemId, boolean ignoreCache);
    @NotNull
    Set<Long> getRoomPretixIds();
    @NotNull
    Map<String, String> getRoomNamesFromRoomPretixItemId(long roomPretixItemId);
    @Nullable
    HotelCapacityPair getRoomInfoFromPretixItemId(long roomPretixItemId);
    @Nullable
    Long getExtraDayItemIdForHotelCapacity(@NotNull String hotelName, @NotNull String roomInternalName,
                                           short capacity, @NotNull ExtraDays day);
    @Nullable
    Long getExtraDayItemIdForHotelCapacity(@NotNull HotelCapacityPair pair, @NotNull ExtraDays day);

    @NotNull
    List<PretixState> getStatesOfCountry(String countryIsoCode);

    @NotNull
    Set<Long> getIdsForItemType(CacheItemTypes type);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromId(long id);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<String> getQuestionIdentifierFromId(long id);

    @NotNull
    Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<Order> parseOrder(@NotNull PretixOrder pretixOrder, @NotNull Event event);

    @Nullable List<PretixQuota> getQuotaFromItemId(long itemId);
    @NotNull
    Optional<PretixQuotaAvailability> getSmallestAvailabilityFromItemId(long itemId);

    void resetCache();

    void reloadAllOrders();
}
