package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\-]{3,64}$")
    private final String internalName;
}
