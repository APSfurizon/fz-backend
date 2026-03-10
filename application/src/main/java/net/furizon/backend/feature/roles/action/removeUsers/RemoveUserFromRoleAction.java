package net.furizon.backend.feature.roles.action.removeUsers;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RemoveUserFromRoleAction {
    boolean invokeMultiple(long roleId, @NotNull Set<Long> userIds);
    boolean invokeSingle(long roleId, long userId);
}
