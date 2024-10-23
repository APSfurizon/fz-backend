package net.furizon.backend.feature.event.action.update;

import net.furizon.backend.feature.event.Event;
import org.jetbrains.annotations.NotNull;

public interface UpdateEventAction {
    void invoke(@NotNull Event event);
}
