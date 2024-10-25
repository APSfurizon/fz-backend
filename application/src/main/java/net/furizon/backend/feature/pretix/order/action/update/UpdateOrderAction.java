package net.furizon.backend.feature.pretix.order.action.update;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface UpdateOrderAction {
    void invoke(@NotNull Order order);
}