package net.furizon.backend.feature.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.dto.LogoutUserResponse;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.dto.RegisterUserResponse;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.feature.authentication.usecase.LogoutUserUseCase;
import net.furizon.backend.feature.authentication.usecase.RegisterUserUseCase;
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

    @PostMapping("/loginn")
    public LoginResponse loginUser(
        @RequestBody final LoginRequest loginRequest,
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
            new LogoutUserUseCase.Input(user.getSessionId())
        );

        return LogoutUserResponse.SUCCESS;
    }


    @PostMapping("/register")
    public RegisterUserResponse registerUser(
        @RequestBody final RegisterUserRequest registerUserRequest
    ) {
        executor.execute(
            RegisterUserUseCase.class,
            registerUserRequest
        );

        return RegisterUserResponse.SUCCESS;
    }
}
