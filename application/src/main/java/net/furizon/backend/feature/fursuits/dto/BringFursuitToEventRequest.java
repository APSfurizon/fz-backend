package net.furizon.backend.feature.fursuits.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class BringFursuitToEventRequest {
    private final boolean bringFursuitToCurrentEvent;

    @Nullable
    private final Long userId;
}
