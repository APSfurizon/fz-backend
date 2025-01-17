package net.furizon.backend.infrastructure.media;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class SimpleImageMetadata {
    private final int width;

    private final int height;

    private final String format;

    private final String type;

    private final byte[] data;
}
