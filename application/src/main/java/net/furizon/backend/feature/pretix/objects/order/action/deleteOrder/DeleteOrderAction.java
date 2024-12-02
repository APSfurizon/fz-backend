package net.furizon.backend.feature.pretix.objects.order.action.deleteOrder;

import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;

public interface DeleteOrderAction {
    void invoke(@NotNull final Order order);

    void invoke(@NotNull final String code);
}
