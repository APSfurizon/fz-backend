package net.furizon.backend.feature.user.action.setBadge;

import org.jetbrains.annotations.Nullable;

public interface SetUserBadgeAction {
    boolean invoke(long userId, @Nullable Long mediaId);
}
