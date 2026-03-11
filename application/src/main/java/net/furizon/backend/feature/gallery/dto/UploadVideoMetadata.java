package net.furizon.backend.feature.gallery.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class UploadVideoMetadata {
    @Nullable private String audioFrequency;
    @Nullable private String videoCodec;
    @Nullable private String audioCodec;
    @Nullable private String framerate;
    private int length;
}
