package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotNull private final String internalName;
}
