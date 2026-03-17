package net.furizon.backend.feature.gallery.usecase.uploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.action.uploadProgress.addUploadProgress.AddUploadProgressAction;
import net.furizon.backend.feature.gallery.action.uploadProgress.deleteUploadProgress.DeleteUploadProgressAction;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.dto.request.StartUploadRequest;
import net.furizon.backend.feature.gallery.dto.response.StartUploadResponse;
import net.furizon.backend.feature.gallery.finder.UploadProgressFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.s3.S3Config;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.s3.dto.MultipartCreationResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.NoSuchUploadException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartUploadUseCase implements UseCase<StartUploadUseCase.Input, StartUploadResponse> {
    @NotNull private final DeleteUploadProgressAction deleteUploadProgress;
    @NotNull private final AddUploadProgressAction addUploadProgress;
    @NotNull private final UploadProgressFinder uploadProgressFinder;
    @NotNull private final S3PresignedUpload s3PresignedUpload;
    @NotNull private final GalleryChecks galleryChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final S3Config s3Config;

    @Override
    @Transactional
    public @NotNull StartUploadResponse executor(@NotNull StartUploadUseCase.Input input) {
        StartUploadRequest req = input.req;
        FurizonUser user = input.user;

        Event event = generalChecks.getEventAndAssertItExists(req.getEventId());
        long userId = galleryChecks.fullUploadChecksAndGetUserId(
                user,
                req.getUserId(),
                event,
                req.getFileSize()
        );
        //We don't need to check if the event is equal between start and stop uploading, since
        // we're gonna make the same checks also on completion time


        //Extract file extension
        final String extension = FilenameUtils.getExtension(req.getFileName());
        //Generate new s3 key for the file
        final String key = UUID.randomUUID().toString() + "." + extension;

        //Fetch any previous upload attempts
        UploadProgress prevAttempt = uploadProgressFinder.getUploadProgressByUser(userId);
        if (prevAttempt != null) {
            log.info("Detected previous upload attempt from user {}. Deleting {} and aborting {}",
                    userId, prevAttempt.getUploadReqId(), prevAttempt.getUploadId());
            //If it exists, delete it
            deleteUploadProgress.invoke(prevAttempt.getUploadReqId());
            //Abort previous upload
            try {
                s3PresignedUpload.abortUpload(prevAttempt.getUploadId(), prevAttempt.getS3Key());
            } catch (NoSuchKeyException | NoSuchUploadException e) {
                log.warn("Unable to abort previous upload for uploadId {} key {} reqId {}: {}",
                    prevAttempt.getUploadId(), prevAttempt.getS3Key(), prevAttempt.getUploadReqId(), e.getMessage());
            }
        }

        // Create new multipart upload
        MultipartCreationResponse multipart = s3PresignedUpload.startMultipart(key, req.getFileSize());
        //Create new upload attempt in db
        long reqId = addUploadProgress.invoke(
                multipart.getUploadId(),
                key,
                multipart.getExpiration(),
                req.getFileSize(),
                userId
        );

        var res = StartUploadResponse.builder()
                .uploadReqId(reqId)
                .s3Endpoint(s3Config.getEndpoint())
                .s3Bucket(s3Config.getBucket())
                .multipartCreationResponse(multipart)
            .build();
        log.info("User {} has created a new multipart upload: {}", userId, res);
        return res;
    }

    public record Input(
            @NotNull StartUploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
