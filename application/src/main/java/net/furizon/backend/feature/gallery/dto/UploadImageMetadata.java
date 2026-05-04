package net.furizon.backend.feature.gallery.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class UploadImageMetadata {
    @Nullable private String cameraMaker;
    @Nullable private String cameraModel;
    @Nullable private String lensMaker;
    @Nullable private String lensModel;
    @Nullable private String focal;
    @Nullable private String shutter;
    @Nullable private String aperture;
    @Nullable private String iso;
}
