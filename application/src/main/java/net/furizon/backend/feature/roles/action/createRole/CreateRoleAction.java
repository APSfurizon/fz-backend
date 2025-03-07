package net.furizon.backend.feature.roles.action.createRole;

import org.jetbrains.annotations.NotNull;

public interface CreateRoleAction {
    long invoke(@NotNull String roleName);
}
