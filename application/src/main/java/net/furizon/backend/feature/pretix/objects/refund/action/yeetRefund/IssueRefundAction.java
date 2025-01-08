package net.furizon.backend.feature.pretix.objects.refund.action.yeetRefund;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface IssueRefundAction {
    boolean invoke(
            @NotNull Event event,
            String orderCode,
            String comment,
            String amount
    );
}
