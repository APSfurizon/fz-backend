package net.furizon.backend.feature.gallery.action.processor.handleJob;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.uploads.updateUploadMetadata.UpdateUploadMetadataAction;
import net.furizon.backend.feature.gallery.action.uploads.upsertImageMetadata.UpsertImageMetadataAction;
import net.furizon.backend.feature.gallery.action.uploads.upsertVideoMetadata.UpsertVideoMetadataAction;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorUploadData;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.action.AddMediaAction;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.action.UpdateMediaMimeTypeAction;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.generated.enums.UploadType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor

public class GalleryProcessorHandleJobImpl implements GalleryProcessorHandleJobAction {
    @NotNull private final UpdateUploadMetadataAction updateUploadMetadataAction;
    @NotNull private final UpdateMediaMimeTypeAction updateMediaMimeTypeAction;
    @NotNull private final UpsertImageMetadataAction upsertImageMetadataAction;
    @NotNull private final UpsertVideoMetadataAction upsertVideoMetadataAction;
    @NotNull private final DeleteMediaAction deleteMediaAction;
    @NotNull private final AddMediaAction addMediaAction;
    @NotNull private final S3DeleteUpload s3DeleteUpload;

    @NotNull private final UploadFinder uploadFinder;


    @Override
    @Transactional
    public boolean invoke(@NotNull GalleryProcessorJob job) {
        boolean res = true;
        long id = job.getId();
        log.info("Updating metadata for job {}", id);

        Long mainMediaId = uploadFinder.getMainMediaIdFromUploadId(id);
        if (job.getType() == UploadType.UNKNOWN) {
            log.error("File type for job {} is unknown. Deleting the file", id);

            if (mainMediaId != null) {
                deleteMediaAction.deleteFromDb(List.of(mainMediaId));
                //deleteUploadAction.invoke(id); ON CASCADE
            }
            String fileName = job.getFile();
            if (fileName != null) {
                s3DeleteUpload.delete(job.getFile());
            }
        }
        GalleryProcessorUploadData data = job.getResult();
        if (data == null) {
            log.error("Data is null for job {}", id);
            return false;
        }

        //Update mime type of main media object
        res = res && mainMediaId != null && updateMediaMimeTypeAction.invoke(mainMediaId, data.getMimeType());

        //Create media object for thumbnail
        Long thumbnailMediaId = null;
        String extraMediaMimeType = data.getExtraMediaMimeType();
        if (res) {
            thumbnailMediaId = addMediaAction.invoke(
                data.getThumbnailMediaName(),
                extraMediaMimeType,
                StoreMethod.S3_REMOTE
            );
            res = thumbnailMediaId >= 0L;
        }
        //Eventually create media object for rendered
        Long renderMediaId = null;
        String renderMediaName = data.getRenderedMediaName();
        if (renderMediaName != null && res) {
            renderMediaId = addMediaAction.invoke(
                renderMediaName,
                extraMediaMimeType,
                StoreMethod.S3_REMOTE
            );
            res = renderMediaId >= 0L;
        }

        //Update metadata
        res = res && updateUploadMetadataAction.invoke(
            job,
            thumbnailMediaId,
            renderMediaId
        );

        //create/update objects for the photo and video metadata
        UploadImageMetadata photoMetadata = data.getPhotoMetadata();
        if (photoMetadata != null) {
            res = res && upsertImageMetadataAction.invoke(
                id,
                photoMetadata
            );
        }
        UploadVideoMetadata videoMetadata = data.getVideoMetadata();
        if (videoMetadata != null) {
            res = res && upsertVideoMetadataAction.invoke(
                id,
                videoMetadata
            );
        }

        if (!res) {
            log.error("Error updating job {}: res was false", id);
            throw new ApiException("Error updating job", GeneralResponseCodes.GENERIC_ERROR);
        }

        return res;
    }
}
