package net.furizon.backend.feature.fursuits.action.updateFursuit;

import org.jetbrains.annotations.NotNull;

public interface UpdateFursuitAction {
    boolean invoke(
        long fursuitId,
        @NotNull String name,
        @NotNull String species
    );
}
