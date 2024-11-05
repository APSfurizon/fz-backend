package net.furizon.backend.feature.user.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.feature.user.usecase.GetUserSessionsUseCase;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UseCaseExecutor executor;

    @GetMapping("/me")
    public FurizonUser getMe(
        @AuthenticationPrincipal @NotNull FurizonUser user
    ) {
        return user;
    }

    @GetMapping("/me/sessions")
    public List<UserSession> getMeSessions(
        @AuthenticationPrincipal @NotNull FurizonUser user
    ) {
        return executor.execute(
            GetUserSessionsUseCase.class,
            new GetUserSessionsUseCase.Input(user.getUserId())
        );
    }
}
