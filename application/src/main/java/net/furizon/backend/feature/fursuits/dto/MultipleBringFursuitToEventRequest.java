package net.furizon.backend.feature.fursuits.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
public class MultipleBringFursuitToEventRequest {
    @NotNull
    private final Map<Long, Boolean> fursuitBroughtToEventMap;

    @Nullable
    private final Long ownerUserId;
}
