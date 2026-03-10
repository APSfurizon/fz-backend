package net.furizon.backend.feature.roles.action.addUsers;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.requests.UpdateRoleToUserRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AddUserToRoleAction {
    boolean invokeMultiple(long roleId, @NotNull List<UpdateRoleToUserRequest> users, @NotNull Event event);
    boolean invokeSingle(long roleId, @NotNull UpdateRoleToUserRequest user, @NotNull Event event);
    boolean invokeSingle(long roleId, long userId, boolean isTempRole, @NotNull Event event);
}
