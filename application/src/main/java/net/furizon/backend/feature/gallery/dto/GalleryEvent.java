package net.furizon.backend.feature.gallery.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class GalleryEvent {
    @NotNull private final Event event;
    @Nullable private final MediaResponse cardDisplayMedia;
    @Nullable private final MediaResponse cardThumbnailMedia;
    private final int photoNumber;
}
