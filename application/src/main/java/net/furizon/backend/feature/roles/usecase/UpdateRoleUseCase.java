package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.roles.action.addPermissoins.AddPermissionsAction;
import net.furizon.backend.feature.roles.action.deletePermissions.DeletePermissionsAction;
import net.furizon.backend.feature.roles.action.updateRoleInfo.UpdateRoleInformationAction;
import net.furizon.backend.feature.roles.dto.UpdateRoleRequest;
import net.furizon.backend.feature.roles.dto.UpdateRoleToUserRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqUserHasRole;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRoleUseCase implements UseCase<UpdateRoleUseCase.Input, Boolean> {
    @NotNull private final PermissionFinder permissionFinder;

    @NotNull private final UpdateRoleInformationAction updateRoleInfoAction;
    @NotNull private final AddPermissionsAction addPermissionsAction;
    @NotNull private final DeletePermissionsAction deletePermissionsAction;

    @Override
    @Transactional
    public @NotNull Boolean executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        long roleId = input.roleId;
        UpdateRoleRequest req = input.request;
        log.info("User {} is updating role {} to '{}'", userId, roleId, req);

        //Fetch saved  data
        Set<Permission> savedPermissions = permissionFinder.getPermissionsFromRoleId(roleId)
                                                           .stream()
                                                           .map(JooqPermission::getPermission)
                                                           .collect(Collectors.toSet());
        Map<Long, JooqUserHasRole> userHasRole = permissionFinder.getUserHasRoleByRoleId(roleId).stream().collect(
                Collectors.toMap(JooqUserHasRole::getUserId, Function.identity())
        );
        Set<Long> savedUsers = userHasRole.keySet();
        //Fetch new data
        Set<Permission> newPermissions = req.getEnabledPermissions();
        Map<Long, UpdateRoleToUserRequest> newUserData = req.getUsers().stream().collect(
                Collectors.toMap(UpdateRoleToUserRequest::getUserId, Function.identity())
        );
        Set<Long> newUsers = newUserData.keySet();

        //Calc which users and permissions needs to be deleted/added (the diff)
        Set<Permission> toDeletePermissions = new HashSet<>(savedPermissions);
        toDeletePermissions.removeAll(newPermissions);
        Set<Permission> toAddPermissions = new HashSet<>(newPermissions);
        toAddPermissions.removeAll(savedPermissions);

        Set<Long> toDeleteUsers = new HashSet<>(savedUsers);
        toDeleteUsers.removeAll(newUsers);
        Set<Long> toAddUsers = new HashSet<>(newUsers);
        toAddUsers.removeAll(savedUsers);

        //Check which user needs their status updated
        List<UpdateRoleToUserRequest> userHasRoleToUpdate = new ArrayList<>(newUsers.size());
        for (UpdateRoleToUserRequest updated : newUserData) {
            JooqUserHasRole old = userHasRole.get(updated.getUserId());
            if (old != null) {
                if (
                   updated.getTempRole() != (old.getTempEventId() != null)
                ) {
                    userHasRoleToUpdate.add(updated);
                }
            }
        }

        //Perform the actual queries
        boolean res;

        //Uptade role data
        res = updateRoleInfoAction.invoke(
                roleId,
                req.getRoleInternalName(),
                req.getRoleDisplayName(),
                req.getShowInAdminCount()
        );
        if (!res) {
            log.error("Error #1 updating role {} to '{}'", roleId, req);
            throw new ApiException("Unable to update permission");
        }

        if (!toDeletePermissions.isEmpty()) {
            log.info("User {} is deleting permissions '{}' to role {}", userId, toDeletePermissions, roleId);
            res = deletePermissionsAction.invoke(roleId, toDeletePermissions);
            if (!res) {
                log.error("Error #2 updating role {} to '{}'", roleId, req);
                throw new ApiException("Unable to update permission");
            }
        }
        if (!toAddPermissions.isEmpty()) {
            log.info("User {} is adding permissions '{}' to role {}", userId, toAddPermissions, roleId);
            res = addPermissionsAction.invoke(roleId, toAddPermissions);
            if (!res) {
                log.error("Error #3 updating role {} to '{}'", roleId, req);
                throw new ApiException("Unable to update permission");
            }
        }
        if (!toDeleteUsers.isEmpty()) {

        }
        if (!toAddUsers.isEmpty()) {

        }

        if (!userHasRoleToUpdate.isEmpty()) {

        }

        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Long roleId,
            @NotNull UpdateRoleRequest request
    ) {}
}
