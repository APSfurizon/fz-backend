package net.furizon.backend.feature.roles.action.deletePermissions;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.infrastructure.security.permissions.Permission;

import java.util.Set;

public interface DeletePermissionsAction {
    boolean invoke(long roleId, @NotNull Set<Permission> permissions);
}
