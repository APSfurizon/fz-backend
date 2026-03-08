package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.action.removeUsers.RemoveUserFromRoleAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveUserFromRoleUseCase implements UseCase<RemoveUserFromRoleUseCase.Input, Boolean> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final RemoveUserFromRoleAction removeUserFromRoleAction;

    @Override
    public @NotNull Boolean executor(@NotNull RemoveUserFromRoleUseCase.Input input) {
        FurizonUser reqUser = input.user;
        long destUserId = input.userId;
        long roleId = input.roleId;

        log.info("User {} is removing user {} from role {}", reqUser.getUserId(), destUserId, roleId);

        if (permissionFinder.userHasRole(destUserId, roleId)) {
            log.warn("User {} already has role {}", destUserId, roleId);
            return true;
        }

        return removeUserFromRoleAction.invokeSingle(roleId, destUserId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Long roleId,
            @NotNull Long userId,
            @NotNull Event event
    ) {}
}
