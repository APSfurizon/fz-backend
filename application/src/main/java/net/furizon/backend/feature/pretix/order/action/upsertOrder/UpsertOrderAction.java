package net.furizon.backend.feature.pretix.order.action.upsertOrder;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface UpsertOrderAction {
    void invoke(@NotNull Order order);
}
