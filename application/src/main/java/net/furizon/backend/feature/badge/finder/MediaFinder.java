package net.furizon.backend.feature.badge.finder;

import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MediaFinder {
    @Nullable
    Set<MediaData> findByIds(Set<Long> ids);
}
