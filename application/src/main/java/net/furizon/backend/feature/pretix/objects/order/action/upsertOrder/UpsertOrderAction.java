package net.furizon.backend.feature.pretix.objects.order.action.upsertOrder;

import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;

public interface UpsertOrderAction {
    void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    );
}
