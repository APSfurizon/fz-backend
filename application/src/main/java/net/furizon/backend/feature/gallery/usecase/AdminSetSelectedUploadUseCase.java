package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.GalleryErrorCodes;
import net.furizon.backend.feature.gallery.action.uploads.updateSelectedUpload.UpdateSelectedUploadAction;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSetSelectedUploadUseCase implements UseCase<AdminSetSelectedUploadUseCase.Input, GalleryUpload> {
    @NotNull
    private final GalleryChecks galleryChecks;
    @NotNull
    private final TranslationService translationService;
    @NotNull
    private final UpdateSelectedUploadAction updateSelectedUploadAction;

    @Override
    public @NotNull GalleryUpload executor(@NotNull AdminSetSelectedUploadUseCase.Input input) {
        log.info("User {} is setting upload {} selection = {}",
                input.user.getUserId(), input.uploadId, input.isSelected);
        GalleryUpload upload = galleryChecks.getUploadAndAssertItExists(input.uploadId);

        if (input.isSelected && upload.getType() != UploadType.IMAGE) {
            log.warn("Upload {} is not an image (type = {})", input.uploadId, upload.getType());
            throw new ApiException(
                    translationService.error("gallery.upload.selection.not_photo"),
                    GalleryErrorCodes.UPLOADS_SELECTION_MUST_BE_PHOTO
            );
        }

        updateSelectedUploadAction.invoke(input.uploadId, input.isSelected);

        upload.setSelected(input.isSelected);
        return upload;
    }

    public record Input(
            long uploadId,
            boolean isSelected,
            @NotNull FurizonUser user
    ) {

    }
}
