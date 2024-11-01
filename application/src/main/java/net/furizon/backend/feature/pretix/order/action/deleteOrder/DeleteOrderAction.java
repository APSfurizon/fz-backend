package net.furizon.backend.feature.pretix.order.action.deleteOrder;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface DeleteOrderAction {
    void invoke(@NotNull final Order order);

    void invoke(@NotNull final String code);
}
