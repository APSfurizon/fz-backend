package net.furizon.backend.feature.gallery.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
@Builder
public class GalleryUploadPreview {
    private final long id;

    private final long photographerUserId;
    //@NotNull private final String photograferName;

    @NotNull private final OffsetDateTime uploadDate;

    @NotNull private final UploadStatus status;

    @NotNull private final String fileName;
    @NotNull private final UploadType type;

    @Nullable private final MediaResponse thumbnailMedia;
    private boolean isSelected;

    private long eventId;
}
