package net.furizon.backend.infrastructure.media.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class MediaResponse {
    private final long mediaId;
    @NotNull private final String mediaUrl;
    @NotNull private final String mimeType;
}
