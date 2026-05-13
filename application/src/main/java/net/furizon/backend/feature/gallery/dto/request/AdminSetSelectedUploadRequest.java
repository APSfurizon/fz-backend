package net.furizon.backend.feature.gallery.dto.request;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AdminSetSelectedUploadRequest {
    @NotNull private final Long uploadId;
    @NotNull private final Boolean selected;
}
