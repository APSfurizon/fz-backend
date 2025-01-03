package net.furizon.backend.feature.pretix.objects.payment.action.yeetPayment;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface IssuePaymentAction {
    boolean invoke(@NotNull Event event, String orderCode, String comment, String amount);
}
