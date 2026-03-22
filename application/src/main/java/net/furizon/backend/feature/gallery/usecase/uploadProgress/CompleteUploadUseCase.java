package net.furizon.backend.feature.gallery.usecase.uploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.GalleryErrorCodes;
import net.furizon.backend.feature.gallery.action.processor.submitJob.GalleryProcessorSubmitJobAction;
import net.furizon.backend.feature.gallery.action.uploadProgress.createUploadAction.CreateUploadAction;
import net.furizon.backend.feature.gallery.action.uploadProgress.deleteUploadProgress.DeleteUploadProgressAction;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.dto.request.CompleteUploadRequest;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteUploadUseCase implements UseCase<CompleteUploadUseCase.Input, GalleryUpload> {
    @NotNull private final CreateUploadAction createUploadAction;
    @NotNull private final DeleteUploadProgressAction deleteUploadProgress;
    @NotNull private final GalleryProcessorSubmitJobAction galleryProcessorSubmitJobAction;
    @NotNull private final S3PresignedUpload s3PresignedUpload;
    @NotNull private final S3DeleteUpload s3DeleteUpload;
    @NotNull private final UploadFinder uploadFinder;
    @NotNull private final GalleryChecks galleryChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull GalleryUpload executor(@NotNull Input input) {
        CompleteUploadRequest req = input.req;
        FurizonUser user = input.user;

        Event event = generalChecks.getEventAndAssertItExists(req.getEventId());
        long userId = galleryChecks.fullUploadChecksAndGetUserId(
                user,
                req.getUserId(),
                event,
                req.getFileSize()
        );

        UploadProgress upload = galleryChecks.getUploadProgressAndAssertItExists(req.getUploadReqId(), userId);

        log.info("Completing multipart upload {} with uploadId {}", req.getUploadReqId(), upload.getUploadId());
        String md5 = s3PresignedUpload.completeMultipart(
                upload.getUploadId(),
                upload.getS3Key(),
                req.getEtags()
        );

        if (!md5.equals(req.getMd5Hash())) {
            log.error("Upload {} (uId {}): md5 hash mismatch! S3 returned {} while req contained {}. "
                    + "Deleting the newly made upload",
                    req.getUploadReqId(), upload.getUploadId(), md5, req.getMd5Hash());
            s3DeleteUpload.delete(upload.getS3Key());
            deleteUploadProgress.invoke(upload.getUploadReqId());
            throw new ApiException(
                translationService.error("gallery.upload.hash_mismatch"),
                GalleryErrorCodes.UPLOADS_HASH_MISMATCH
            );
        }

        Long hashCollision = uploadFinder.getUploadIdByHashOnEvent(md5, event.getId());
        if (hashCollision != null) {
            log.error("Upload {} (uId {}): Duplicate upload detected for hash {}. "
                    + "Deleting the newly made upload",
                    req.getUploadReqId(), upload.getUploadId(), md5);
            s3DeleteUpload.delete(upload.getS3Key());
            deleteUploadProgress.invoke(upload.getUploadReqId());
            throw new ApiException(
                    translationService.error("gallery.upload.duplicated"),
                    GalleryErrorCodes.UPLOADS_DUPLICATE
            );
        }

        //Create media object in the db
        GalleryUpload ret = createUploadAction.invoke(
            user.getUserId(),
            userId,
            req.getFileName(),
            req.getFileSize(),
            req.getUploadRepostPermissions(),
            event,
            upload.getS3Key(),
            MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
            StoreMethod.S3_REMOTE,
            md5
        );

        deleteUploadProgress.invoke(upload.getUploadReqId());

        //Launch processor job
        try {
            galleryProcessorSubmitJobAction.invoke(ret.getId(), upload.getS3Key());
        } catch (Exception e) {
            log.warn("Upload {}: Unable to submit job to processor",  upload.getUploadReqId());
        }

        return ret;
    }

    public record Input(
            @NotNull CompleteUploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
