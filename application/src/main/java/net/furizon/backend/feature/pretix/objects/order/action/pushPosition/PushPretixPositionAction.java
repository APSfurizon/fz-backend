package net.furizon.backend.feature.pretix.objects.order.action.pushPosition;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PushPretixPositionAction {
    @Nullable
    PretixPosition invoke(
            @NotNull Event event,
            @NotNull PushPretixPositionRequest position
    );
}
