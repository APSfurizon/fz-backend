package net.furizon.backend.feature.pretix.objects.order.action.updatePosition;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import org.jetbrains.annotations.NotNull;

public interface UpdatePretixPositionAction {
    PretixPosition invoke(
            @NotNull Event event,
            long positionId,
            boolean checkQuota,
            @NotNull final UpdatePretixPositionRequest position
    );
}
