package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    private final StoreMethod storeMethod;
}
