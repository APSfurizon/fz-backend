package net.furizon.backend.feature.pretix.objects.payment.finder;

import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixPaymentFinder {
    PretixPaging<PretixPayment> getPagedPayments(
            @NotNull String organizer,
            @NotNull String event,
            int page
    );
}
