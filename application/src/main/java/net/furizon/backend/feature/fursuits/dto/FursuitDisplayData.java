package net.furizon.backend.feature.fursuits.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class FursuitDisplayData {
    private final long id;
    @NotNull private String name;
    @NotNull private String species;
    @Nullable private MediaResponse propic;
    private final boolean bringingToEvent;

    private boolean showInFursuitCount;

    private final long ownerId;

    @Nullable private final Sponsorship sponsorship;
}
