package net.furizon.backend.feature.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.requests.ChangePasswordRequest;
import net.furizon.backend.feature.authentication.dto.requests.EmailRequest;
import net.furizon.backend.feature.authentication.dto.requests.LoginRequest;
import net.furizon.backend.feature.authentication.dto.requests.RegisterUserRequest;
import net.furizon.backend.feature.authentication.dto.responses.AuthenticationCodeResponse;
import net.furizon.backend.feature.authentication.dto.responses.LoginResponse;
import net.furizon.backend.feature.authentication.dto.responses.LogoutUserResponse;
import net.furizon.backend.feature.authentication.dto.responses.RegisterUserResponse;
import net.furizon.backend.feature.authentication.usecase.ChangePasswordUseCase;
import net.furizon.backend.feature.authentication.usecase.ConfirmEmailUseCase;
import net.furizon.backend.feature.authentication.usecase.GetResetPwStatusUseCase;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.feature.authentication.usecase.LogoutUserUseCase;
import net.furizon.backend.feature.authentication.usecase.RegisterUserUseCase;
import net.furizon.backend.feature.authentication.usecase.ResetPasswordUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequiredMode;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UseCaseExecutor executor;
    private final PretixInformation pretixService;

    @GetMapping("/test")
    @PermissionRequired(permissions = {Permission.CAN_UPGRADE_USERS}, mode = PermissionRequiredMode.ANY)
    public boolean testPermission(
            @AuthenticationPrincipal @Nullable final FurizonUser user
    ) {
        return true;
    }

    @PostMapping("/login")
    public LoginResponse loginUser(
        @AuthenticationPrincipal @Nullable final FurizonUser user,
        @Valid @NotNull @RequestBody final LoginRequest loginRequest,
        @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) @Nullable String userAgent,
        HttpServletRequest httpServletRequest
    ) {
        return executor.execute(
            LoginUserUseCase.class,
            new LoginUserUseCase.Input(
                user,
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                httpServletRequest.getRemoteAddr(),
                userAgent
            )
        );
    }

    @PostMapping("/logout")
    public LogoutUserResponse logoutUser(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        executor.execute(
            LogoutUserUseCase.class,
            user.getSessionId()
        );

        return LogoutUserResponse.SUCCESS;
    }


    @Operation(summary = "Registers a new user", description =
        "Birthday and residence countries should be passed as a ISO 3166-1 A-2 string. "
        + "Birthday and residence state instead must correspond to the `code` field obtained "
        + "with the [`/states/by-country`](#/pretix-states-controller/getPretixStates) endpoint. "
        + "If a state is unsupported (the previous endpoint returns an empty array), the user "
        + "should be prompted with a normal text field which can be normally submitted as state")
    @PostMapping("/register")
    public RegisterUserResponse registerUser(
            HttpServletRequest request,
            @Valid @NotNull @RequestBody final RegisterUserRequest registerUserRequest
    ) {
        executor.execute(
            RegisterUserUseCase.class,
            new RegisterUserUseCase.Input(
                request,
                registerUserRequest,
                pretixService.getCurrentEvent()
            )
        );

        return RegisterUserResponse.SUCCESS;
    }

    @Operation(summary = "Changes the user's password", description =
        "This method can be used both for changing the password of an already "
        + "logged in user and both for resetting the password after a password "
        + "reset flow. If the user is already logged in, you MUST omit the "
        + "`resetPwId` parameter of the body, if instead the user is resetting "
        + "his password (so he's not logged in) that parameter is *mandatory*")
    @PostMapping("/pw/change")
    public void changePw(
            @AuthenticationPrincipal @Nullable final FurizonUser user,
            @Valid @NotNull @RequestBody final ChangePasswordRequest req
    ) {
        executor.execute(
                ChangePasswordUseCase.class,
                new ChangePasswordUseCase.Input(user, req)
        );
    }

    @Operation(summary = "Checks the status of a password reset flow", description =
        "This method is meant to be used by the frontend after an user has loaded "
        + "the page of the password reset, so it can check if the request is still "
        + "valid and let the user proceed. We return `PW_RESET_STILL_PENDING` if "
        + "the reset flow is still valid and the user *can* change its password "
        + "from the reset link, `PW_RESET_NOT_FOUND` otherwise. We return "
        + "a `ALREADY_LOGGED_IN` if the user is already logged and he doesn't "
        + "need to reset its password")
    @GetMapping("/pw/reset/status")
    public AuthenticationCodeResponse getPwResetStatus(
            @AuthenticationPrincipal @Nullable final FurizonUser user,
            @Valid @NotNull @RequestParam("id") final UUID id
    ) {
        return executor.execute(
                GetResetPwStatusUseCase.class,
                new GetResetPwStatusUseCase.Input(user, id)
        );
    }

    @Operation(summary = "Initialize a password reset flow", description =
        "By calling this method the user will receive an email with a link to "
        + "reset its password. An user can instantiate only one flow per time, "
        + "but no error is returned both if a flow is already active and both "
        + "if the email doesn't exists, to not leak users email")
    @PostMapping("/pw/reset")
    public boolean resetPw(
            @Valid @NotNull @RequestBody final EmailRequest req
    ) {
        return executor.execute(
                ResetPasswordUseCase.class,
                req
        );
    }

    @Operation(summary = "Confirms an email and enables an account", description =
        "This endpoint SHOULD NOT be implemented in the frontend. Users will open a link directly to this page "
        + "and, on success, this page should redirect back to the login page with a code in the "
        + "url's `status` param with the result")
    @GetMapping("/confirm-mail")
    public RedirectView confirmEmail(
        @Valid @RequestParam("id") @NotNull final UUID id
    ) {
        return executor.execute(
                ConfirmEmailUseCase.class,
                id
        );
    }
}
