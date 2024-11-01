package net.furizon.backend.feature.pretix.event.action.update;

import net.furizon.backend.feature.pretix.event.Event;
import org.jetbrains.annotations.NotNull;

public interface UpdateEventAction {
    void invoke(@NotNull final Event event);
}
