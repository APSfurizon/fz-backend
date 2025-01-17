package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@Builder
public class MediaData {
    @NotNull
    private final Long id;

    @NotNull
    private final String path;

    @NotNull
    private final String mediaType;

    @NotNull
    private final StoreMethod storeMethod;
}
