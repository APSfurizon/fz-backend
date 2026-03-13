package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.action.deleteUploadProgress.DeleteUploadProgressAction;
import net.furizon.backend.feature.gallery.dto.request.StartUploadRequest;
import net.furizon.backend.feature.gallery.dto.response.StartUploadResponse;
import net.furizon.backend.feature.gallery.finder.UploadProgressFinder;
import net.furizon.backend.infrastructure.s3.actions.presignedUpload.S3PresignedUpload;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartUploadUseCase implements UseCase<StartUploadUseCase.Input, StartUploadResponse> {
    @NotNull private final DeleteUploadProgressAction deleteUploadProgress;
    @NotNull private final UploadProgressFinder uploadProgressFinder;
    @NotNull private final S3PresignedUpload s3PresignedUpload;
    @NotNull private final GalleryChecks galleryChecks;

    @Override
    @Transactional
    public @NotNull StartUploadResponse executor(@NotNull StartUploadUseCase.Input input) {
        StartUploadRequest req = input.req;
        FurizonUser user = input.user;

        long userId = galleryChecks.fullUploadChecksAndGetUserId(user, req);
        //We don't need to check if the event is equal between start and stop uploading, since
        // we're gonna make the same checks also on completion time


        //Extract file extension
        final String extension = FilenameUtils.getExtension(req.getFileName());
        //Generate new s3 key for the file
        final String key = UUID.randomUUID().toString() + "." + extension;

        //Fetch any previous upload attempts
        Long prevAttemptId = uploadProgressFinder.getUploadingProgressIdByUser(userId);
        if (prevAttemptId != null) {
            log.info("Detected previous upload attempt from user {}. Deleting {}", userId, prevAttemptId);
            //If it exists, delete it
            deleteUploadProgress.invoke(prevAttemptId);
        }

        //Abort previous upload
        s3PresignedUpload.abortUpload();
        // Create new multipart upload

        //Create new upload attempt in db

        return null;
    }

    public record Input(
            @NotNull StartUploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
