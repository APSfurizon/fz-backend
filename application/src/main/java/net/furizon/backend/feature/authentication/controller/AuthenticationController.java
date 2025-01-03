package net.furizon.backend.feature.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.dto.LogoutUserResponse;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.dto.RegisterUserResponse;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.feature.authentication.usecase.LogoutUserUseCase;
import net.furizon.backend.feature.authentication.usecase.RegisterUserUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UseCaseExecutor executor;
    private final PretixInformation pretixService;

    @PostMapping("/login")
    public LoginResponse loginUser(
        @Valid @RequestBody final LoginRequest loginRequest,
        @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) @Nullable String userAgent,
        HttpServletRequest httpServletRequest
    ) {
        return executor.execute(
            LoginUserUseCase.class,
            new LoginUserUseCase.Input(
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
        @Valid @RequestBody final RegisterUserRequest registerUserRequest
    ) {
        executor.execute(
            RegisterUserUseCase.class,
            new RegisterUserUseCase.Input(
                registerUserRequest,
                pretixService.getCurrentEvent().orElse(null)
            )
        );

        return RegisterUserResponse.SUCCESS;
    }
}
