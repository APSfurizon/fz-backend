package net.furizon.backend.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.feature.user.dto.UserAdminViewData;
import net.furizon.backend.feature.user.dto.UsersByIdResponse;
import net.furizon.backend.feature.user.objects.dto.UserDisplayDataResponse;
import net.furizon.backend.feature.user.usecase.GetUserAdminViewDataUseCase;
import net.furizon.backend.feature.user.usecase.GetUserDisplayDataUseCase;
import net.furizon.backend.feature.user.usecase.GetUserSessionsUseCase;
import net.furizon.backend.feature.user.usecase.SearchUsersByIdsUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @NotNull private final GetUserAdminViewDataUseCase getUserAdminViewDataUseCase;

    @GetMapping("/me")
    public FurizonUser getMe(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return user;
    }

    @GetMapping("/display/me")
    public Optional<UserDisplayDataResponse> getMeDisplay(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetUserDisplayDataUseCase.class,
                new GetUserDisplayDataUseCase.Input(
                        user.getUserId(),
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Obtains a DisplayUserData for multiple, specified, users", description =
        "Provide a comma separated list of user ids in the `id` field. This endpoint will return "
        + "a list of DisplayUserData, one for each found id, which contains all the information needed to "
        + "display an user on the frontend")
    @GetMapping("/display/by-id")
    public UsersByIdResponse searchUsersByIds(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @NotNull
            @Size(min = 1)
            @RequestParam("id")
            final String[] userIds
    ) {
        return executor.execute(
                SearchUsersByIdsUseCase.class,
                new SearchUsersByIdsUseCase.Input(
                        userIds,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @GetMapping("/me/sessions")
    public List<UserSession> getMeSessions(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
            GetUserSessionsUseCase.class,
            user.getUserId()
        );
    }
    @Operation(summary = "View user", description =
        "This operation can be performed only by an admin. "
        + "Returns all the information we have regarding a user. The frontend page should let the admin "
        + "to change the following data, using the dedicated endpoints: personalInfo, register/unregister "
        + "membership cards, if an user is banned or not, currentRoomData management (create, invite, kick,"
        + "leave, un/confirm, delete, name change, show in nosecount, accept/refuse/cancel invitations, "
        + "buy/upgrade room), create/manage room and order exchanges, change nickname and badge photo, "
        + "fursuit management (creation, deletion, bringToCurrentEvent, chane name and species, "
        + "show in nosecount, change propic), add and remove roles, link new orders by code. "
        + "For every order, a buttons should be displayed that, once clicked, it retrieves the link from "
        + "the endpoint `orders-workflow/generate-pretix-control-order-link` "
        + "which redirects the admin to the order's pretix control page. "
        + "When this page is loaded, a call to /admin/capabilities must be done to check which operations "
        + "are currently permitted to the admin.")
    @PermissionRequired(permissions = {Permission.CAN_VIEW_USER, Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @GetMapping("/view/{id}")
    public UserAdminViewData getUserAdminViewData(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("id") Long userId
    ) {
        return getUserAdminViewDataUseCase.execute(
                user,
                userId,
                pretixInformation.getCurrentEvent(),
                pretixInformation
        );
    }
}
