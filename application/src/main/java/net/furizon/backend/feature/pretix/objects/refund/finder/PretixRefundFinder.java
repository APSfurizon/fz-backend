package net.furizon.backend.feature.pretix.objects.refund.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.refund.PretixRefund;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PretixRefundFinder {
    PretixPaging<PretixRefund> getPagedRefunds(
            @NotNull String organizer,
            @NotNull String event,
            @NotNull String orderCode,
            int page
    );

    List<PretixRefund> getRefundsForOrder(
            @NotNull Event event,
            @NotNull String orderCode
    );
}
