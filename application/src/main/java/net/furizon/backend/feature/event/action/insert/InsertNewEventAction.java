package net.furizon.backend.feature.event.action.insert;

import net.furizon.backend.feature.event.Event;
import org.jetbrains.annotations.NotNull;

public interface InsertNewEventAction {
    void invoke(@NotNull Event event);
}
