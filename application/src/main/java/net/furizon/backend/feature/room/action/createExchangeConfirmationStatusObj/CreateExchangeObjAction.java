package net.furizon.backend.feature.room.action.createExchangeConfirmationStatusObj;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface CreateExchangeObjAction {
    long invoke(long targetUserId, long sourceUserId, @NotNull Event event);
}
