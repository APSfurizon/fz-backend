package net.furizon.backend.feature.roles.action.removeUsers;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RemoveUsersFromRoleAction {
    boolean invoke(long roleId, @NotNull Set<Long> userIds);
}
