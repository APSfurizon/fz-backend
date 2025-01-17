package net.furizon.backend.feature.pretix.objects.order.finder.pretix;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.feature.pretix.objects.refund.PretixRefund;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface PretixBalanceForProviderFinder {
    @NotNull Map<String, Long> get(@NotNull String orderCode, @NotNull Event event,
                                   boolean crashOnInvalidState);
    @NotNull Map<String, Long> get(@NotNull List<PretixPayment> payments, @NotNull List<PretixRefund> refunds,
                                      @NotNull String orderCode, @NotNull Event event, boolean crashOnInvalidState);
}
