package net.furizon.backend.feature.pretix.objects.product.finder;

import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixProductFinder {
    @NotNull
    PretixPaging<PretixProduct> getPagedProducts(
            @NotNull String organizer,
            @NotNull String event,
            int page
    );
}
