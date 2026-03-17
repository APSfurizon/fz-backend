package net.furizon.backend.feature.gallery.dto.processor;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class GalleryProcessorUploadData {
    private int resolutionWidth;
    private int resolutionHeight;
    @Nullable private OffsetDateTime shotTimestamp;

    private long fileSize;
    @NotNull private String mimeType;

    @NotNull private String extraMediaMimeType;
    @NotNull private String thumbnailMediaName;
    @Nullable private String renderedMediaName;

    @Nullable private UploadImageMetadata photoMetadata;
    @Nullable private UploadVideoMetadata videoMetadata;
}
