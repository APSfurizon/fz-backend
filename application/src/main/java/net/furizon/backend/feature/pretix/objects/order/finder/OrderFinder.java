package net.furizon.backend.feature.pretix.objects.order.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderFinder {
    @NotNull Set<String> findOrderCodesForEvent(@NotNull Event event);
    @NotNull Set<Long> findOrderIdsForEvent(@NotNull Event event);

    @Nullable
    Order findOrderByCodeEvent(@NotNull String code, @NotNull Event event, @NotNull PretixInformation pretixService);

    @Nullable
    Order findOrderByUserIdEvent(long userId, @NotNull Event event, @NotNull PretixInformation pretixService);

    int countOrdersOfUserOnEvent(long userId, @NotNull Event event);

    //This checks directly on the capacity, so NO_ROOM item is still counted as not having a room
    @NotNull
    Optional<Boolean> userHasBoughtAroom(long userId, @NotNull Event event);
    
    //The optional is empty if no event for the user is found!
    @NotNull
    Optional<Boolean> isOrderDaily(long userId, @NotNull Event event);

    Optional<OrderStatus> getOrderStatus(long userId, @NotNull Event event);

    @Nullable
    OrderDataResponse getOrderDataResponseFromUserEvent(long userId, @NotNull Event event,
                                                        @NotNull PretixInformation pretixService);

    @Nullable
    Short getBoughtExtraFursuits(long userId, @NotNull Event event);

    @Nullable String getOrderCodeById(long orderId);

    @Nullable Long getOrderIdByCode(@NotNull String orderCode, @NotNull Event event);

    @NotNull List<Order> getUnlinkedOrder(@NotNull PretixInformation pretixService, @NotNull Event event);
}
