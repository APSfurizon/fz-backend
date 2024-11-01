package net.furizon.backend.feature.pretix.event.finder;

import net.furizon.backend.feature.pretix.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EventFinder {
    @Nullable
    Event findEventBySlug(@NotNull String slug);
}
