package net.furizon.backend.feature.pretix.objects.order.action.addPositionWithPayment;

import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AddPositionWithPaymentAction {
    boolean invoke(@NotNull Order order,
                   @NotNull PretixInformation pretixInformation,

                   long itemId,
                   boolean createPayment,
                   int quantity,
                   @Nullable Long addonToPositionId,
                   @Nullable Long variationId,
                   @Nullable Long price,
                   boolean checkQuotas,
                   boolean notifyUser);
}
