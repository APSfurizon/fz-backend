package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleToUserRequest {
    @NotNull private final Long userId;
    @NotNull private final Boolean tempRole;
}
