package net.furizon.backend.feature.badge.action.updateUserBadge;

import org.jetbrains.annotations.NotNull;

public interface UpdateUserBadgeAction {
    boolean invoke(
            long userId,
            @NotNull String fursonaName,
            @NotNull String locale
    );
}
