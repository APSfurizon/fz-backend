package net.furizon.backend.feature.gallery.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Builder
public class GalleryUpload {
    private final long id;
    @NotNull private final UserDisplayData photographer;

    @NotNull private final LocalDateTime uploadDate;
    @Nullable private final LocalDateTime shotDate;

    @NotNull private final UploadStatus status;

    @NotNull private final String fileName;
    private final long fileSize;
    private final int width;
    private final int height;
    @NotNull private final UploadType type;

    @NotNull private final MediaResponse media;
    @Nullable private final MediaResponse renderedMedia;
    @Nullable private final MediaResponse thumbnailMedia;

    private boolean isSelected;

    @NotNull private final UploadRepostPermissions repostPermissions;

    @NotNull private final Event event;

    @Nullable private UploadImageMetadata photoMetadata;
    @Nullable private UploadVideoMetadata videoMetadata;
}
