package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadFile;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadFile;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectOnConditionStep;

import java.util.List;
import java.util.Set;

public interface UploadFinder {
    int countUserUploadsOnEvent(long userId, @NotNull Event event);

    @Nullable Long getPhotographerUserId(long uploadId);

    @Nullable Long getOriginalUploaderUserId(long uploadId);

    @Nullable Long getMainMediaIdFromUploadId(long uploadId);

    @Nullable String getMainMediaFilenameFromUploadId(long uploadId);

    @Nullable Long getThumbnailMediaIdFromUploadId(long uploadId);

    @Nullable String getThumbnailMediaFilenameFromUploadId(long uploadId);

    @Nullable Long getRenderMediaIdFromUploadId(long uploadId);

    @Nullable String getRenderMediaFilenameFromUploadId(long uploadId);

    @Nullable Long getUploadIdByHash(@NotNull String hash);

    @Nullable Long getUploadIdByHashOnEvent(@NotNull String hash, long eventId);

    @NotNull List<Long> getUnprocessedUploadIds();

    @Nullable GalleryUpload getUploadById(long uploadId);

    @NotNull List<GalleryUploadPreview> adminBatchApprovalRetrival(
            long fromId,
            long limit,
            List<Pair<Long, Long>> reservedRanges
    );

    @NotNull List<GalleryUploadPreview> listPreview(
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @Nullable UploadStatus uploadStatus,
            @Nullable Long reqUserId,
            boolean isReqUserAnAdmin,
            long fromId,
            boolean before,
            long limit
    );

    @NotNull SelectOnConditionStep<?> selectPreviewUploadObj();

    @NotNull
    JooqUploadFinder.FullUploadObjSelected selectFullUploadObj();

    @Nullable GalleryEvent getGalleryEvent(long eventId, @Nullable Long photographerId);

    @NotNull List<GalleryEvent> getGalleryEvents(@Nullable Long photographerId);

    @NotNull JooqUploadFinder.GalleryEventObjSelected selectGalleryEventObj(@Nullable Long photographerId);

    @Nullable GalleryPhotographer getGalleryPhotographer(long photographerId, @Nullable Long eventId);

    @NotNull List<GalleryPhotographer> getGalleryPhotographers(@Nullable Long eventId);

    JooqUploadFinder.GalleryPhotographerObjSelected selectGalleryPhotographerObj(@Nullable Long eventId);

    @Nullable Long getFirstPendingUpload(long startFrom);

    int countPendingUploads();

    @NotNull List<BulkDownloadFile> getBulkDownloadableFiles(Set<Long> ids);

    @NotNull List<BulkDirectDownloadFile> getBulkDirectDownloadableFiles(Set<Long> ids);
}
