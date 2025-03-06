package net.furizon.backend.feature.roles.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.infrastructure.security.permissions.Permission;

import java.util.List;
import java.util.Set;

@Data
public class UpdateRoleRequest {
    @NotNull private final String roleInternalName;
    @Nullable private final String roleDisplayName;

    @NotNull private final Boolean showInAdminCount;

    @NotNull private final Set<Permission> enabledPermissions;

    @NotNull private final List<UpdateRoleToUserRequest> users;
}
