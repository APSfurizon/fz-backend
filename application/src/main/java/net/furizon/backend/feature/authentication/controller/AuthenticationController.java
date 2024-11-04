package net.furizon.backend.feature.authentication.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.dto.RegisterUserResponse;
import net.furizon.backend.feature.authentication.usecase.CreateLoginSessionUseCase;
import net.furizon.backend.feature.authentication.usecase.RegisterUserUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UseCaseExecutor executor;

    @PostMapping("/login")
    public LoginResponse loginUser(
        @RequestBody final LoginRequest loginRequest
    ) {
        return executor.execute(
            CreateLoginSessionUseCase.class,
            loginRequest
        );
    }

    @PostMapping("/register")
    public RegisterUserResponse registerUser(
        @RequestBody final RegisterUserRequest registerUserRequest
    ) {
        executor.execute(
            RegisterUserUseCase.class,
            registerUserRequest
        );

        return new RegisterUserResponse(true);
    }
}
