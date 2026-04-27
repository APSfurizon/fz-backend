package net.furizon.backend.feature.fursuits.action.updateFursuit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UpdateFursuitAction {
    boolean invoke(
        long fursuitId,
        @NotNull String name,
        @Nullable String species,
        boolean showInFursuitCount,
        boolean showOwner
    );
}
