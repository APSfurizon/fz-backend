package net.furizon.backend.feature.pretix.objects.order.action.deletePosition;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface DeletePretixPositionAction {
    boolean invoke(
            @NotNull Event event,
            long positionId
    );
}
