package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class RoleResponse {
    private final long roleId;

    @NotNull private final String internalName;
    @Nullable private final String displayName;

    private final boolean showInAdminCount;

    @NotNull private final Set<Permission> enabledPermissions;

    @NotNull private final List<UserHasRoleResponse> users;
}
