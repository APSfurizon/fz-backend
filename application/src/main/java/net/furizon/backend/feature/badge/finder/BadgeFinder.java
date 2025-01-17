package net.furizon.backend.feature.badge.finder;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.Nullable;

public interface BadgeFinder {
    @Nullable MediaData getMediaDataOfUserBadge(long userId);
}
