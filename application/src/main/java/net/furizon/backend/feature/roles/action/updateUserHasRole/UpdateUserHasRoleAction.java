package net.furizon.backend.feature.roles.action.updateUserHasRole;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.UpdateRoleToUserRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface UpdateUserHasRoleAction {
    boolean invoke(long roleId, @NotNull List<UpdateRoleToUserRequest> updateReqs, @NotNull Event event);
}
