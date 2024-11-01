package net.furizon.backend.feature.pretix.order.action.updateOrder;

import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;

public interface UpdateOrderAction {
    void invoke(@NotNull Order order, @NotNull PretixInformation pretixInformation);
}
