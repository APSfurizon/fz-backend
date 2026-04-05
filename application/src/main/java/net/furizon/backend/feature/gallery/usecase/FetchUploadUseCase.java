package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUploadUseCase implements UseCase<FetchUploadUseCase.Input, GalleryUpload> {
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final TranslationService translationService;
    @NotNull
    private final GalleryChecks galleryChecks;

    @Override
    public @NotNull GalleryUpload executor(@NotNull FetchUploadUseCase.Input input) {
        GalleryUpload upload = galleryChecks.getUploadAndAssertItExists(input.uploadId);

        if (upload.getStatus() != UploadStatus.APPROVED) {
            if (input.user == null) {
                log.error("Upload {} is in status {} and user is anonymous",
                        input.uploadId, upload.getStatus());
                throw new ApiException(
                        translationService.error("gallery.not_owner_of_upload"),
                        GeneralResponseCodes.USER_IS_NOT_ADMIN
                );
            }
            long reqUserId = input.user.getUserId();
            if (upload.getPhotographer().getUserId() != reqUserId) {
                var canManage = permissionFinder.userHasPermission(reqUserId, Permission.UPLOADS_CAN_MANAGE_UPLOADS);
                if (!canManage) {
                    log.error("Upload {} is in status {} and user {} is neither the owner not an admin",
                            input.uploadId, upload.getStatus(), reqUserId);
                    throw new ApiException(
                        translationService.error("gallery.not_owner_of_upload"),
                        GeneralResponseCodes.USER_IS_NOT_ADMIN
                    );
                }
            }
        }

        return upload;
    }

    public record Input(
        long uploadId,
        @Nullable FurizonUser user
    ) {}
}
