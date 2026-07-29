package net.furizon.backend.feature.fursuits.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class FursuitListResponse {
    @NotNull
    private final List<FursuitData> fursuits;
    private final short bringingToEvent;
    private final short maxFursuitsBroughtToEvent;
    private final short maxExtraFursuitBadges;

    private final boolean canBringFursuitsToEvent;

    private final boolean allowEditBringFursuitToEvent;
}
