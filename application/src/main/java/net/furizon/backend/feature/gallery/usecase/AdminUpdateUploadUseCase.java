package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.uploads.adminUpdateUpload.AdminUpdateUploadAction;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUpdateUploadUseCase implements UseCase<AdminUpdateUploadUseCase.Input, Boolean> {
    @NotNull
    private final GeneralChecks checks;
    @NotNull
    private final TranslationService translationService;
    @NotNull
    private final AdminUpdateUploadAction adminUpdateUploadAction;
    @NotNull
    private final UserFinder userFinder;

    @Override
    public @NotNull Boolean executor(@NotNull AdminUpdateUploadUseCase.Input input) {
        log.info("User {} is changing upload details. status={}, photographer={}, event={}, uids={}",
                input.user.getUserId(), input.status, input.photographerId, input.eventId, input.uploadIds);

        if (input.eventId != null) {
            checks.getEventAndAssertItExists(input.eventId);
        }
        if (input.photographerId != null) {
            User user = userFinder.findById(input.photographerId);
            if (user == null) {
                log.error("Photographer {} not found", input.photographerId);
                throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    translationService.error("user.not_found"),
                    GeneralResponseCodes.USER_NOT_FOUND
                );
            }
        }

        if (input.eventId == null
                && input.photographerId == null
                && input.status == null
                && input.repostPermissions == null) {
            log.debug("Empty update request, returning");
            return true;
        }

        return adminUpdateUploadAction.invoke(
                input.uploadIds,
                input.status,
                input.repostPermissions,
                input.photographerId,
                input.eventId
        );
    }

    public record Input(
            List<Long> uploadIds,
            @Nullable UploadStatus status,
            @Nullable UploadRepostPermissions repostPermissions,
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @NotNull FurizonUser user
    ) {}
}
