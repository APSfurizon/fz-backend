package net.furizon.backend.feature.gallery.dto.processor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GalleryProcessorJobSubmit { //NewJobRequest
    private final long id;
    @NotNull private final String file;

}
