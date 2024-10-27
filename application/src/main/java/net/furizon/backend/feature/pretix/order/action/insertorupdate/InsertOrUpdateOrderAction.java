package net.furizon.backend.feature.pretix.order.action.insertorupdate;

import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;

public interface InsertOrUpdateOrderAction {
    void invoke(@NotNull Order order, @NotNull PretixInformation pretixInformation);
}
