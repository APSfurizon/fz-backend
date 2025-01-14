package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@Builder
public class MediaData {
    @NotNull
    private final Long id;

    @NotNull
    private final String relativePath;

    @NotNull
    private final String mediaType;
}
