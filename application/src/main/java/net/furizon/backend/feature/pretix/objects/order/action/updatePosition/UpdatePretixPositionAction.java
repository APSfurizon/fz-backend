package net.furizon.backend.feature.pretix.objects.order.action.updatePosition;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import org.jetbrains.annotations.NotNull;

public interface UpdatePretixPositionAction {
    boolean invoke(
            @NotNull Event event,
            long positionId,
            @NotNull final UpdatePretixPositionRequest position
    );
}
