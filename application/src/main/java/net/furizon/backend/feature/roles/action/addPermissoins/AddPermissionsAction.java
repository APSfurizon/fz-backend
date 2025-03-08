package net.furizon.backend.feature.roles.action.addPermissoins;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.infrastructure.security.permissions.Permission;

import java.util.Set;

public interface AddPermissionsAction {
    boolean invoke(long roleId, @NotNull Set<Permission> permissions);
}
