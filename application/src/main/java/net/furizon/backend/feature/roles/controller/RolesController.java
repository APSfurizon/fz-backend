package net.furizon.backend.feature.roles.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.roles.dto.CreateRoleRequest;
import net.furizon.backend.feature.roles.dto.ListingPermissionsResponse;
import net.furizon.backend.feature.roles.dto.ListingRolesResponse;
import net.furizon.backend.feature.roles.dto.RoleIdResponse;
import net.furizon.backend.feature.roles.dto.RoleResponse;
import net.furizon.backend.feature.roles.dto.UpdateRoleRequest;
import net.furizon.backend.feature.roles.usecase.CreateRoleUseCase;
import net.furizon.backend.feature.roles.usecase.DeleteRoleUseCase;
import net.furizon.backend.feature.roles.usecase.FetchRoleUseCase;
import net.furizon.backend.feature.roles.usecase.ListRolesUseCase;
import net.furizon.backend.feature.roles.usecase.UpdateRoleUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolesController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;

    @Operation(summary = "List all the possible permissions in the backend", description =
        "This method returns all the supported permissions in the backend. They should be displayed directly by "
        + "their enum's name, and not translated in any way. When an admin wants to add a permission to a role, "
        + "the frontend should contact this page to fetch the full permission list, and then search inside the list "
        + "for the permission the admin is searching")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @GetMapping("/permissions")
    public @NotNull ListingPermissionsResponse listPossiblePermissions(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return ListingPermissionsResponse.INSTANCE;
    }

    @Operation(summary = "List all the created roles", description =
        "This method returns the list of roles created in the furpanel. For each one, we have: "
        + "the internal name (which is mandatory), the display name (optional, may be used in admin counts), "
        + "if the role is showed in the admin count, the number of users who has this rule permanently and "
        + "temporarily and the number of permissions which has this role")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @GetMapping("/")
    public @NotNull ListingRolesResponse listRoles(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(ListRolesUseCase.class, user);
    }

    @Operation(summary = "Creates a new role", description =
        "To create a new role, it's mandatory to specify an internal name. "
        + "The internal name must satisfy the regex `^[A-Za-z0-9_\\-]{3,64}$`."
        + "This method returns back the id of the newly created role, the frontend should then "
        + "redirect the user to the page to edit that role")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @PostMapping("/")
    public @NotNull RoleIdResponse createRole(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody CreateRoleRequest request
    ) {
        log.info("User {} is creating role '{}'", user, request.getInternalName());
        return executor.execute(CreateRoleUseCase.class, request);
    }


    @Operation(summary = "Fetch all info about the specified role", description =
        "This method returns all the data regarding the role: Its internal name (mandatory), "
        + "its display name (optional, used for admin count), if the role is shown in the admin count, the set "
        + "of linked permissions to this role and the list of users who has this role. "
        + "Since a role can be set temporarily for a user, together with each user, there's also a "
        + "flag which tells us if the permissions is temporary or not. Permissions names should not be translated "
        + "by the frontend")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @GetMapping("/{roleId}")
    public @NotNull RoleResponse fetchRole(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("roleId") @NotNull final Long roleId
    ) {
        return executor.execute(FetchRoleUseCase.class, roleId);
    }

    @Operation(summary = "Fully updates the specified role", description =
        "This method updates every data of the specified role. Partial updates are currently unsupported, "
        + "so you have to provide  the full role object. See the GetRole method for an explanation about the data. "
        + "The internal name must satisfy the regex `^[A-Za-z0-9_\\-]{3,64}$`.")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @PostMapping("/{roleId}")
    public boolean updateRole(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("roleId") @NotNull final Long roleId,
            @Valid @NotNull @RequestBody final UpdateRoleRequest request
    ) {
        return executor.execute(
                UpdateRoleUseCase.class,
                new UpdateRoleUseCase.Input(
                        user,
                        roleId,
                        request,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Deletes the specified role")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS})
    @DeleteMapping("/{roleId}")
    public boolean deleteRole(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("roleId") @NotNull final Long roleId
    ) {
        log.info("User {} is deleting role {}", user.getUserId(), roleId);
        return executor.execute(DeleteRoleUseCase.class, roleId);
    }
}
