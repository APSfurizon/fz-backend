package net.furizon.backend.feature.fursuits.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FursuitDataRequest {
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$")
    @NotNull private final String name;
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$")
    @NotNull private final String species;

    @NotNull private final Boolean bringToCurrentEvent;

    @NotNull private final Boolean showInFursuitCount;

    @Nullable private final Long userId;
}
