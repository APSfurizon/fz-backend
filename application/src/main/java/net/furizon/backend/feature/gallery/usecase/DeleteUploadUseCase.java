package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.GalleryErrorCodes;
import net.furizon.backend.feature.gallery.action.uploads.deleteUpload.DeleteUploadAction;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteUploadUseCase implements UseCase<DeleteUploadUseCase.Input, Boolean> {
    @NotNull
    private final MediaFinder mediaFinder;
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final PermissionFinder permissionFinder;

    @NotNull
    private final DeleteMediaAction deleteMediaAction;
    //@NotNull
    //private final DeleteUploadAction deleteUploadAction;
    @NotNull
    private final S3DeleteUpload s3DeleteUpload;

    @NotNull
    private final GalleryChecks checks;
    @NotNull
    private final TranslationService translationService;

    @Override
    @Transactional
    public @NotNull Boolean executor(@NotNull DeleteUploadUseCase.Input input) {
        long userId = input.user.getUserId();
        long uploadId = input.uploadId;
        log.info("User {} is permanently deleting upload {}", userId, uploadId);

        Long photographer = uploadFinder.getPhotographerUserId(uploadId);
        checks.assertUploadFound(photographer);

        if (photographer != userId) {
            boolean b = permissionFinder.userHasPermission(userId, Permission.UPLOADS_CAN_FULLY_DELETE_UPLOADS);
            if (!b) {
                log.error("User {} is trying to delete upload {}, "
                        + "but he's not the owner nor he has the correct permissions!",
                        userId, uploadId);
                throw new ApiException(
                        translationService.error("gallery.not_owner_of_upload"),
                        GeneralResponseCodes.USER_IS_NOT_ADMIN
                );
            }
        }

        long mainMediaId = uploadFinder.getMainMediaIdFromUploadId(uploadId);
        Long thumbnailMediaId = uploadFinder.getThumbnailMediaIdFromUploadId(uploadId);
        Long renderMediaId = uploadFinder.getRenderMediaIdFromUploadId(uploadId);

        String mainMediaKey = mediaFinder.getPathById(mainMediaId);
        String thumbnailMediaKey = thumbnailMediaId == null ? null : mediaFinder.getPathById(thumbnailMediaId);
        String renderMediaKey = renderMediaId == null ? null : mediaFinder.getPathById(renderMediaId);


        boolean res = true;

        List<Long> ids = new ArrayList<>(3);
        ids.add(mainMediaId);
        if (thumbnailMediaId != null) {
            ids.add(thumbnailMediaId);
        }
        if (renderMediaId != null) {
            ids.add(renderMediaId);
        }
        res = res && deleteMediaAction.deleteFromDb(ids);
        //Upload object is deleted with cascade

        res = res && mainMediaKey != null;
        if (res) {
            s3DeleteUpload.delete(mainMediaKey);
            if (thumbnailMediaKey != null) {
                s3DeleteUpload.delete(thumbnailMediaKey);
            }
            if (renderMediaKey != null) {
                s3DeleteUpload.delete(renderMediaKey);
            }
        }

        if (!res) {
            log.error("Deleting of upload {} failed! Res was false", uploadId);
            throw new ApiException(
                translationService.error("common.server_error"),
                GeneralResponseCodes.GENERIC_ERROR
            );
        }

        return res;
    }

    public record Input(
            long uploadId,
            @NotNull FurizonUser user
    ) {}
}
