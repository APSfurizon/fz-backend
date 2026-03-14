package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.gallery.dto.UploadProgress;
import org.jetbrains.annotations.Nullable;

public interface UploadProgressFinder {
    @Nullable Long getUploadingProgressIdByUser(long userId);

    @Nullable UploadProgress getUploadProgressByUser(long userId);

    @Nullable UploadProgress getUploadProgressByReqId(long reqId);

    @Nullable UploadProgress getUploadProgressByReqIdUser(long reqId, long userId);
}
