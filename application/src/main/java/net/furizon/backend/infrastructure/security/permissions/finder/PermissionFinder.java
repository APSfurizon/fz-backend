package net.furizon.backend.infrastructure.security.permissions.finder;

import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PermissionFinder {

    @NotNull List<JooqPermission> getPermissionsFromRoleId(long roleId);

    @NotNull List<JooqPermission> getPermissionsFromRoleInternalName(@NotNull String roleInternalName);

    @Nullable Role getRoleFromId(long roleId);

    @Nullable Role getRoleFromInternalName(@NotNull String roleInternalName);

    boolean userHasRole(long userId, long roleId);

    boolean userHasRole(long userId, @NotNull String roleInternalName);

    boolean userHasPermission(long userId, @NotNull Permission permission);
}