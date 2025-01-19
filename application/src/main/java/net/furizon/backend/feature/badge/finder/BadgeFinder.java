package net.furizon.backend.feature.badge.finder;

import net.furizon.backend.infrastructure.media.dto.MediaData;
import org.jetbrains.annotations.Nullable;

public interface BadgeFinder {
    @Nullable MediaData getMediaDataOfUserBadge(long userId);

    @Nullable MediaData getMediaDataOfFursuitBadge(long fursuitId);
}
