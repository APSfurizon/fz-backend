package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.action.addUsers.AddUserToRoleAction;
import net.furizon.backend.feature.roles.dto.requests.UpdateRoleToUserRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddUserToRoleUseCase implements UseCase<AddUserToRoleUseCase.Input, Boolean> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final AddUserToRoleAction addUsersToRoleAction;

    @Override
    public @NotNull Boolean executor(@NotNull AddUserToRoleUseCase.Input input) {
        FurizonUser reqUser = input.user;
        long destUserId = input.request.getUserId();
        boolean temp = input.request.getTempRole();
        long roleId = input.roleId;

        log.info("User {} is adding user {} to role {}", reqUser.getUserId(), destUserId, roleId);

        if (permissionFinder.userHasRole(destUserId, roleId)) {
            log.warn("User {} already has role {}", destUserId, roleId);
            return true;
        }

        return addUsersToRoleAction.invokeSingle(roleId, destUserId, temp, input.event);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Long roleId,
            @NotNull UpdateRoleToUserRequest request,
            @NotNull Event event
    ) {}
}
