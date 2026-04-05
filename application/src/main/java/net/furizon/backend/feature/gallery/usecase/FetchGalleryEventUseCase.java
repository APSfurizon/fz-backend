package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchGalleryEventUseCase implements UseCase<FetchGalleryEventUseCase.Input, GalleryEvent> {
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final GeneralChecks checks;

    @Override
    public @NotNull GalleryEvent executor(@NotNull FetchGalleryEventUseCase.Input input) {
        var v = uploadFinder.getGalleryEvent(input.eventId, input.photographerId);
        if (v == null) {
            Event event = checks.getEventAndAssertItExists(input.eventId);
            return new GalleryEvent(
                event,
                null,
                null,
                0
            );
        }
        return v;
    }

    public record Input(
        @Nullable Long photographerId,
        long eventId
    ) {}
}
