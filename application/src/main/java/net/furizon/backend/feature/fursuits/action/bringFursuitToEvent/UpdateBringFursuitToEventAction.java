package net.furizon.backend.feature.fursuits.action.bringFursuitToEvent;

import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UpdateBringFursuitToEventAction {
    boolean invoke(long fursuitId, boolean bringing, @NotNull Order linkedOrder);
}
