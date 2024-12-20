package net.furizon.backend.feature.pretix.objects.order.finder.pretix;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixPositionFinder {
    @NotNull Optional<PretixPosition> fetchPositionById(@NotNull Event event, final long positionId);
}
