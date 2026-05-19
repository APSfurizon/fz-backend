package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.processor.retryJob.GalleryProcessorRetryJobAction;
import net.furizon.backend.feature.gallery.action.uploads.setUploadType.SetUploadTypeAction;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.media.usecase.RemoveDanglingMediaUseCase;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReprocessUploadUseCase implements UseCase<ReprocessUploadUseCase.Input, Boolean> {
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final DeleteMediaAction deleteMediaAction;
    @NotNull
    private final SetUploadTypeAction setUploadTypeAction;

    @NotNull
    private final GalleryProcessorRetryJobAction galleryProcessorRetryJobAction;

    @Override
    @Transactional
    public @NotNull Boolean executor(@NotNull ReprocessUploadUseCase.Input input) {
        try {
            RemoveDanglingMediaUseCase.mediaWriteMutexLockException();
            log.info("User {} is triggering reprocessing of uploads {}", input.user.getUserId(), input.uploadIds);

            List<Long> mediaToDelete = new ArrayList<>();
            var uploads = uploadFinder.getUploadByIds(input.uploadIds);
            for (var upload : uploads.values()) {
                long uploadId = upload.getId();
                if (upload.getType() == UploadType.UNPROCESSED) {
                    log.warn("Upload {} is not processed, skipping", uploadId);
                    continue;
                }

                MediaResponse displayMedia = upload.getDisplayMedia();
                if (displayMedia != null && displayMedia.getMediaId() != uploadId) {
                    mediaToDelete.add(displayMedia.getMediaId());
                }
                MediaResponse thumbnailMedia = upload.getThumbnailMedia();
                if (thumbnailMedia != null && thumbnailMedia.getMediaId() != uploadId) {
                    mediaToDelete.add(thumbnailMedia.getMediaId());
                }
            }
            if (!mediaToDelete.isEmpty()) {
                //We don't want to delete old thumbnails, they're going to be replaced and
                // eventually the dangling media removal will delete the old files
                //deleteMediaAction.deleteFromDb(mediaToDelete);
            }
            setUploadTypeAction.invoke(input.uploadIds, UploadType.UNPROCESSED);

            boolean success = true;
            for (long uploadId : input.uploadIds) {
                try {
                    galleryProcessorRetryJobAction.invoke(uploadId);
                } catch (Exception e) {
                    log.warn("Error while re-submitting job for upload {}", uploadId, e);
                    success = false;
                }
            }

            return success;
        } finally {
            RemoveDanglingMediaUseCase.mediaWriteMutexUnlock();
        }
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Set<Long> uploadIds
    ) {}
}
