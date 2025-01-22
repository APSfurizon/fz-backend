package net.furizon.backend.feature.fursuits.action.createFursuit;

import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CreateFursuitAction {
    long invoke(
            long ownerId,
            @NotNull String name,
            @NotNull String species,
            boolean showInFursuitCount,
            @Nullable Order linkedOrder
    );
}
