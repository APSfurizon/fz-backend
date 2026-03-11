package net.furizon.backend.feature.gallery.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Builder
public class GalleryProcessorUploadData {
    private int resolutionWidth;
    private int resolutionHeight;
    @Nullable private LocalDateTime shotTimestamp;

    private long hash;
    private long fileSize;
    @NotNull private String mimeType;

    @NotNull private String thumbnailMediaName;
    @Nullable private String renderedMediaName;

    @Nullable private UploadImageMetadata photoMetadata;
    @Nullable private UploadVideoMetadata videoMetadata;
}
