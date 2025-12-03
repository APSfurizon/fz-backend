package net.furizon.backend.feature.pretix.objects.product.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.PretixProductVariation;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixVariationFinder {
    @NotNull
    PretixPaging<PretixProductVariation> getPagedVariations(
            @NotNull String organizer,
            @NotNull String event,
            long itemId,
            int page
    );

    @NotNull Optional<PretixProductVariation> fetchVariationById(@NotNull Event event, long itemId, long variationId);
}
