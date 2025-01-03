package net.furizon.backend.feature.pretix.objects.payment.action.manualCancelPayment;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface ManualCancelPaymentAction {
    boolean invoke(@NotNull Event event, String orderCode, long paymentId);

}
