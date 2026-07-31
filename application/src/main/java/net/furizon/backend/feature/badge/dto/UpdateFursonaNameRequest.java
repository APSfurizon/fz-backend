package net.furizon.backend.feature.badge.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import net.furizon.backend.infrastructure.GeneralConsts;

@Data
public class UpdateFursonaNameRequest {
    @Nullable
    private final Long userId;

    @Pattern(regexp = GeneralConsts.NAME_REGEX)
    @NotNull
    private final String fursonaName;
}
