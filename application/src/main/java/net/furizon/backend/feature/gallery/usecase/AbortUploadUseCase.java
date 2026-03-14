package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.action.deleteUploadProgress.DeleteUploadProgressAction;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.dto.request.S3UploadRequest;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbortUploadUseCase implements UseCase<AbortUploadUseCase.Input, Boolean> {
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final GalleryChecks galleryChecks;
    @NotNull private final DeleteUploadProgressAction deleteUploadProgressAction;
    @NotNull private final S3PresignedUpload s3PresignedUpload;

    @Override
    @Transactional
    public @NotNull Boolean executor(@NotNull Input input) {
        final S3UploadRequest req = input.req;
        final FurizonUser user = input.user;

        long userId = generalChecks.getUserIdAndAssertPermission(
                req.getUserId(),
                user,
                Permission.UPLOADS_CAN_MANAGE_UPLOADS
        );

        UploadProgress prevUpload = galleryChecks.getUploadProgressAndAssertItExists(
                req.getUploadReqId(),
                userId
        );

        log.info("User {} is deleting prev multipart upload attempt {} (uploadId = {})",
                userId, prevUpload.getUploadReqId(), prevUpload.getUploadId());

        deleteUploadProgressAction.invoke(req.getUploadReqId());

        s3PresignedUpload.abortUpload(prevUpload.getUploadId(), prevUpload.getS3Key());

        return true;
    }

    public record Input(
            @NotNull S3UploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
