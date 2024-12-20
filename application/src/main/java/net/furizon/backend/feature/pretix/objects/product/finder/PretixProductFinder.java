package net.furizon.backend.feature.pretix.objects.product.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixProductFinder {
    @NotNull
    PretixPaging<PretixProduct> getPagedProducts(
            @NotNull String organizer,
            @NotNull String event,
            int page
    );

    @NotNull Optional<PretixProduct> fetchProductById(@NotNull final Event event, final long itemId);
}
