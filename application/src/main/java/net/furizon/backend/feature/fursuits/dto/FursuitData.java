package net.furizon.backend.feature.fursuits.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FursuitData {
    private final boolean bringingToEvent;
    private final long ownerId;

    private boolean showInFursuitCount;

    private final FursuitDisplayData fursuit;
}
