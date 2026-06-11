package net.furizon.backend.feature.gallery.dto.processor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.Nullable;

@Data
public class GalleryProcessorJob {
    @NotNull private final Long id;
    @Nullable private final String file;
    @NotNull private final GalleryProcessorJobStatus status;
    @Nullable private final UploadType type; //job type
    @Nullable private final GalleryProcessorUploadData result;
}
