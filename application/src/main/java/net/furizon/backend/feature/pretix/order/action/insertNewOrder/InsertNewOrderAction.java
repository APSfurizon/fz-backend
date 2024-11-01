package net.furizon.backend.feature.pretix.order.action.insertNewOrder;

import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;

public interface InsertNewOrderAction {
    void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    );
}
