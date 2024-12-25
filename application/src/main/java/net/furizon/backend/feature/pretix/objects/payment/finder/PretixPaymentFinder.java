package net.furizon.backend.feature.pretix.objects.payment.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PretixPaymentFinder {
    PretixPaging<PretixPayment> getPagedPayments(
            @NotNull String organizer,
            @NotNull String event,
            @NotNull String code,
            int page
    );

    List<PretixPayment> getPaymentsForOrder(
            @NotNull Event event,
            @NotNull String orderCode
    );
}
