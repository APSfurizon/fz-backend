package net.furizon.backend.feature.pretix.event.action.insert;

import net.furizon.backend.feature.pretix.event.Event;
import org.jetbrains.annotations.NotNull;

public interface InsertNewEventAction {
    void invoke(@NotNull Event event);
}
