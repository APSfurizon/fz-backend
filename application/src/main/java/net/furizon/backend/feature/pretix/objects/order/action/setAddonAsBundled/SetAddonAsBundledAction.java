package net.furizon.backend.feature.pretix.objects.order.action.setAddonAsBundled;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface SetAddonAsBundledAction {
    boolean invoke(long positionId, boolean isBundled, @NotNull Event event);
}
