package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryEventsResponse;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryPhotographersResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListGalleryPhotographersUseCase implements UseCase<Long, ListGalleryPhotographersResponse> {
    @NotNull
    private final UploadFinder uploadFinder;

    @Override
    public @NotNull ListGalleryPhotographersResponse executor(@NotNull Long eventId) {
        return new ListGalleryPhotographersResponse(
            uploadFinder.getGalleryPhotographers(eventId < 0L ? null : eventId)
        );
    }
}
