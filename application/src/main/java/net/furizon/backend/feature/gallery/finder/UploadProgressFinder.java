package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.gallery.dto.UploadProgress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UploadProgressFinder {
    @Nullable Long getUploadingProgressIdByUser(long userId);

    @NotNull List<UploadProgress> getExpiredUploadProgress();

    @Nullable UploadProgress getUploadProgressByUser(long userId);

    @Nullable UploadProgress getUploadProgressByReqId(long reqId);

    @Nullable UploadProgress getUploadProgressByReqIdUser(long reqId, long userId);
}
