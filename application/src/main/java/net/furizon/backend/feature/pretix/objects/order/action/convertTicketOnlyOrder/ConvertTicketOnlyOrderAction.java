package net.furizon.backend.feature.pretix.objects.order.action.convertTicketOnlyOrder;

import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConvertTicketOnlyOrderAction {
    boolean invoke(@NotNull Order order,
                   @NotNull PretixInformation pretixInformation,
                   @Nullable UpdateOrderInDb updateOrderInDb);
}
