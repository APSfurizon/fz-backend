package net.furizon.backend.feature.pretix.objects.event.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EventFinder {
    @Nullable Event findEventBySlug(@NotNull String slug);

    @Nullable Event findEventById(long id);

    @NotNull List<Event> getAllEvents();
}
