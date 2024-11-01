package net.furizon.backend.feature.pretix.order.action.insertNewOrder;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface InsertNewOrderAction {
    void invoke(@NotNull Order order);
}
