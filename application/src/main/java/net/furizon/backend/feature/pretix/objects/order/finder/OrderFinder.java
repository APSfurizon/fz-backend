package net.furizon.backend.feature.pretix.objects.order.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface OrderFinder {
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

    @NotNull
    OrderDataResponse getOrderDataResponseFromUserEvent(long userId, @NotNull Event event, @NotNull PretixInformation pretixService);
}
