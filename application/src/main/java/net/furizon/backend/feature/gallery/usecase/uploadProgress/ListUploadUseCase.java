package net.furizon.backend.feature.gallery.usecase.uploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.dto.request.S3UploadRequest;
import net.furizon.backend.feature.gallery.dto.response.ListUploadPartsResponse;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListUploadUseCase implements UseCase<ListUploadUseCase.Input, ListUploadPartsResponse> {
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final GalleryChecks galleryChecks;
    @NotNull private final S3PresignedUpload s3PresignedUpload;


    @Override
    public @NotNull ListUploadPartsResponse executor(@NotNull Input input) {
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

        log.info("User {} is listing prev multipart upload attempt {} (uploadId = {})",
                userId, prevUpload.getUploadReqId(), prevUpload.getUploadId());

        var res = s3PresignedUpload.listParts(prevUpload.getUploadId(), prevUpload.getS3Key());
        return new ListUploadPartsResponse(res);
    }

    public record Input(
            @NotNull S3UploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
