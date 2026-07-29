package net.furizon.backend.feature.badge.action.updateFursonaName;

import org.jetbrains.annotations.NotNull;

public interface UpdateFursonaNameAction {
    boolean invoke(
            long userId,
            @NotNull String fursonaName
    );
}
