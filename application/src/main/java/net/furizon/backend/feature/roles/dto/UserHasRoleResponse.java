package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;

@Data
public class UserHasRoleResponse {
    private final boolean tempRole;

    @NotNull private final UserDisplayData displayData;
}
