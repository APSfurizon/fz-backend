package net.furizon.backend.feature.roles.dto.responses;

import lombok.Data;
import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class RolesResponse {
    @NotNull private final List<Role> roles;
}
