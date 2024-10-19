package net.furizon.backend.feature.authentication;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.dto.LogoutRequest;
import net.furizon.backend.feature.authentication.usecase.CreateAuthenticationTokenUseCase;
import net.furizon.backend.feature.authentication.usecase.DestoySessionUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UseCaseExecutor executor;

    @PostMapping("/login")
    String login() {
        return executor.execute(
            CreateAuthenticationTokenUseCase.class,
            new LoginRequest()
        );
    }

    @PostMapping("/logout")
    Boolean logout(/*@AuthenticationPrincipal FurizonUser user*/) {
        return executor.execute(
            DestoySessionUseCase.class,
            new LogoutRequest(1)
        );
    }
}
