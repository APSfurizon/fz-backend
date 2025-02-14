package net.furizon.backend.feature.pretix.objects.order.action.deleteOrder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface DeleteOrderAction {
    void invoke(@NotNull final Order order);

    void invoke(@NotNull final String code, @NotNull Event event);
    void invoke(long orderId);

    void invokeWithCodes(@NotNull final Set<String> orderCodes, @NotNull Event event);
    void invokeWithIds(@NotNull final Set<Long> orderIds);
}
