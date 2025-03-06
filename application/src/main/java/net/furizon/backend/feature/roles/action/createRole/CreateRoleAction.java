package net.furizon.backend.feature.roles.action.createRole;

import org.jetbrains.annotations.Nullable;

public interface CreateRoleAction {
    long invoke(String roleName);
}
