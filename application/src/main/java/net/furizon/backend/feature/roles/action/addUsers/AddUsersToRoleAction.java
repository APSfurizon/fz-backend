package net.furizon.backend.feature.roles.action.addUsers;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.UpdateRoleToUserRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AddUsersToRoleAction {
    boolean invoke(long roleId, @NotNull List<UpdateRoleToUserRequest> users, @NotNull Event event);
}
