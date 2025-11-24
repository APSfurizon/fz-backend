package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.roles.dto.RoleResponse;
import net.furizon.backend.feature.roles.dto.UserHasRoleResponse;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchRoleUseCase implements UseCase<Long, RoleResponse> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull RoleResponse executor(@NotNull Long input) {
        Role role = permissionFinder.getRoleFromId(input);
        if (role == null) {
            log.error("Role {} not found", input);
            throw new ApiException(translationService.error("roles.role_not_found"),
                    GeneralResponseCodes.ROLE_NOT_FOUND);
        }

        Set<Permission> permissions = role.getPermissions(permissionFinder)
                                          .stream()
                                          .map(JooqPermission::getPermission)
                                          .collect(Collectors.toSet());

        List<UserHasRoleResponse> users = permissionFinder.getDisplayUsersWithRoleId(input);

        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .internalName(role.getInternalName())
                .displayName(role.getDisplayName())
                .roleAdmincountPriority(role.getRoleAdmincountPriority())
                .showInAdminCount(role.isShowInNosecount())
                .enabledPermissions(permissions)
                .users(users)
            .build();
    }
}
