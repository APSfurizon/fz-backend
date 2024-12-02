package net.furizon.backend.feature.pretix.objects.event.action.update;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface UpdateEventAction {
    void invoke(@NotNull final Event event);
}
