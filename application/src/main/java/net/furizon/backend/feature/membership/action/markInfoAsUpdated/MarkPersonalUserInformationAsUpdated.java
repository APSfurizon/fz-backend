package net.furizon.backend.feature.membership.action.markInfoAsUpdated;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface MarkPersonalUserInformationAsUpdated {
    void invoke(long userId, @NotNull Event event);
}
