package net.furizon.backend.feature.gallery.usecase.uploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.uploadProgress.deleteUploadProgress.DeleteUploadProgressAction;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.finder.UploadProgressFinder;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteExpiredUploadProgressUseCase implements UseCase<Integer, Integer> {
    @NotNull private final DeleteUploadProgressAction deleteUploadProgressAction;
    @NotNull private final UploadProgressFinder uploadProgressFinder;
    @NotNull private final S3PresignedUpload s3PresignedUpload;

    @Override
    public @NotNull Integer executor(@NotNull Integer input) {
        List<UploadProgress> pendingUploads = uploadProgressFinder.getExpiredUploadProgress();

        for (UploadProgress uploadProgress : pendingUploads) {
            try {
                log.info("Aborting expired upload progress {} (uploadId = {})",
                        uploadProgress.getUploadReqId(), uploadProgress.getUploadId());
                s3PresignedUpload.abortUpload(uploadProgress.getUploadId(), uploadProgress.getS3Key());
                deleteUploadProgressAction.invoke(uploadProgress.getUploadReqId());
            } catch (Exception e) {
                log.error("Error while deleting expired upload progress {}", uploadProgress.getUploadReqId(), e);
            }
        }
        return pendingUploads.size();
    }
}
