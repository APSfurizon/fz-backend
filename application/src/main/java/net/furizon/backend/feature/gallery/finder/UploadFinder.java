package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record12;
import org.jooq.SelectOnConditionStep;

import java.time.OffsetDateTime;
import java.util.List;

public interface UploadFinder {
    int countUserUploadsOnEvent(long userId, @NotNull Event event);

    @Nullable Long getUploaderUserId(long uploadId);

    @Nullable Long getOriginalUploaderUserId(long uploadId);

    @Nullable Long getMainMediaIdFromUploadId(long uploadId);

    @Nullable String getMainMediaFilenameFromUploadId(long uploadId);

    @Nullable Long getUploadIdByHash(@NotNull String hash);

    @Nullable Long getUploadIdByHashOnEvent(@NotNull String hash, long eventId);

    @NotNull List<Long> getUnprocessedUploadIds();

    @Nullable GalleryUpload getUploadById(long uploadId);

    @NotNull List<GalleryUploadPreview> listPreview(
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @Nullable Long reqUserId,
            boolean isReqUserAnAdmin,
            long fromId,
            long limit
    );

    @NotNull SelectOnConditionStep<?> selectPreviewUploadObj();

    @NotNull
    JooqUploadFinder.FullUploadObjSelected selectFullUploadObj();
}
