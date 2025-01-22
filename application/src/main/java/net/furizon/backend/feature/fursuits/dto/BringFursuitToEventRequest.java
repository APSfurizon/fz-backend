package net.furizon.backend.feature.fursuits.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class BringFursuitToEventRequest {
    @NotNull
    private final Boolean bringFursuitToCurrentEvent;

    @Nullable
    private final Long userId;
}
