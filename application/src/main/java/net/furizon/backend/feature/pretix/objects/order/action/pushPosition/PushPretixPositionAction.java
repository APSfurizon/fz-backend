package net.furizon.backend.feature.pretix.objects.order.action.pushPosition;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import org.jetbrains.annotations.NotNull;

public interface PushPretixPositionAction {
    boolean invoke(
            @NotNull Event event,
            @NotNull PushPretixPositionRequest position
    );
}
