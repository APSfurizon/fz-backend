package net.furizon.backend.feature.pretix.order.action.insertorupdate;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface InsertOrUpdateOrderAction {
    void invoke(@NotNull Order order);
}
