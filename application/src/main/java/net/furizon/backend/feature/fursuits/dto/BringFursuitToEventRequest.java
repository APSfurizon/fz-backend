package net.furizon.backend.feature.fursuits.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BringFursuitToEventRequest {
    @NotNull
    private final Boolean bringFursuitToCurrentEvent;
}
