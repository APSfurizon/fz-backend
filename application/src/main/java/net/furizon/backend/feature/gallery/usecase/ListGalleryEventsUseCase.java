package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryEventsResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListGalleryEventsUseCase implements UseCase<Long, ListGalleryEventsResponse> {
    @NotNull
    private final UploadFinder uploadFinder;

    @Override
    public @NotNull ListGalleryEventsResponse executor(@NotNull Long photographerId) {
        return new ListGalleryEventsResponse(
            uploadFinder.getGalleryEvents(photographerId < 0L ? null : photographerId)
        );
    }
}
