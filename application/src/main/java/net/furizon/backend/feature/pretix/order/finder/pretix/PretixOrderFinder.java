package net.furizon.backend.feature.pretix.order.finder.pretix;

import net.furizon.backend.feature.pretix.order.PretixOrder;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixOrderFinder {
    @NotNull
    PretixPaging<PretixOrder> getPagedOrders(
        @NotNull String organizer,
        @NotNull String event,
        int page
    );

    @NotNull
    Optional<PretixOrder> fetchOrderByCode(
        @NotNull String organizer,
        @NotNull String event,
        @NotNull String code
    );
}
