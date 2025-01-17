package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@Builder
public class BadgeUploadResponse {
    @NotNull
    private final Long id;

    @NotNull
    private final String relativePath;
}
