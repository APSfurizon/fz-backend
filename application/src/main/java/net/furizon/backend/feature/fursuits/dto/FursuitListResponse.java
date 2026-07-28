package net.furizon.backend.feature.fursuits.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class FursuitListResponse {
    @NotNull
    private final List<FursuitData> fursuits;
    private final short bringingToEvent;
    private final short maxFursuits;

    private final boolean canBringFursuitsToEvent;

    private final boolean allowEditBringFursuitToEvent;
}
