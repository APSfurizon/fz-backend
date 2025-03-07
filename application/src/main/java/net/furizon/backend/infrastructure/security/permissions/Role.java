package net.furizon.backend.infrastructure.security.permissions;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class Role {
    private final long roleId;
    @NotNull private final String displayName;
    @Nullable private final String internalName;
    private final boolean showInNosecount;

    @Getter(AccessLevel.NONE)
    private List<JooqPermission> permissions = null;

    @NotNull
    public synchronized List<JooqPermission> getPermissions(@NotNull PermissionFinder finder) {
        if (permissions == null) {
            permissions = finder.getPermissionsFromRoleId(roleId);
        }
        return permissions;
    }

    public boolean hasPermissions(@NotNull Permission permission, @NotNull PermissionFinder finder) {
        var r = getPermissions(finder);
        for (JooqPermission perm : r) {
            if (perm.getPermission() == permission) {
                return true;
            }
        }
        return false;
    }
}
