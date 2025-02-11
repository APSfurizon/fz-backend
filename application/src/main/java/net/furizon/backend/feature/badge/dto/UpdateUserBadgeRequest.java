package net.furizon.backend.feature.badge.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserBadgeRequest {
    @Nullable private final Long userId;

    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$")
    @NotNull private final String fursonaName;
    @Size(min = 2, max = 2)
    @NotEmpty
    @NotNull private final String locale;
}
