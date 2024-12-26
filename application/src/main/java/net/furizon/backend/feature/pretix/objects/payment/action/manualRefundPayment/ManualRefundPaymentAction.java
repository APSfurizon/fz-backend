package net.furizon.backend.feature.pretix.objects.payment.action.manualRefundPayment;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface ManualRefundPaymentAction {
    boolean invoke(@NotNull Event event, String orderCode, long paymentId, long amount);
}
