package net.furizon.backend.feature.fursuits.dto;

import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class FursuitDisplayData {
    private final long id;
    @NotNull private final String name;
    @NotNull private final String species;
    @NotNull private final String propicUrl;
    private final boolean bringingToEvent;
    
    @Nullable private final Sponsorship sponsorship;
}
