package net.furizon.backend.feature.fursuits.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import net.furizon.backend.infrastructure.GeneralConsts;

@Data
public class FursuitDataRequest {
    @Pattern(regexp = GeneralConsts.NAME_REGEX)
    @NotNull private final String name;
    @Pattern(regexp = GeneralConsts.NAME_REGEX)
    @Nullable private final String species;

    @NotNull private final Boolean bringToCurrentEvent;

    @NotNull private final Boolean showInFursuitCount;

    @NotNull private final Boolean showOwner;
  
    @Nullable private final Long userId;
}
