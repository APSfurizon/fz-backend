package net.furizon.backend.feature.fursuits.action.setBadge;

import org.jetbrains.annotations.Nullable;

public interface SetFursuitBadgeAction {
    boolean invoke(long fursuitId, @Nullable Long mediaId);
}
