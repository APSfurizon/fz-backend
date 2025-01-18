package net.furizon.backend.feature.badge.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserBadgeRequest {
    @Nullable private final Long userId;

    @NotNull private final String fursonaName;
    @NotNull private final String locale;
}
