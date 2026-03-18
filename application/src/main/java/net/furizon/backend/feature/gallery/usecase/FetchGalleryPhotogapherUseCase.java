package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchGalleryPhotogapherUseCase implements
        UseCase<FetchGalleryPhotogapherUseCase.Input, GalleryPhotographer> {
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final TranslationService translationService;

    @Override
    public @NotNull GalleryPhotographer executor(@NotNull FetchGalleryPhotogapherUseCase.Input input) {
        var v = uploadFinder.getGalleryPhotographer(input.photographerId, input.eventId);
        if (v == null) {
            UserDisplayData userData = userFinder.getDisplayUser(input.photographerId, null);
            if (userData == null) {
                log.error("Unable to find user {}", input.photographerId);
                throw new ApiException(
                    translationService.error("user.not_found"),
                    GeneralResponseCodes.USER_NOT_FOUND
                );
            }
            return new GalleryPhotographer(
                userData,
                false,
                0
            );
        }
        return v;
    }

    public record Input(
        @Nullable Long eventId,
        long photographerId
    ) {}
}
