package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyAllSessionsUseCase implements UseCase<FurizonUser, Boolean> {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull Boolean executor(@NotNull FurizonUser input) {
        log.info("Clearing all sessions of user {}", input.getUserId());
        sessionAuthenticationManager.clearAllSession(input.getUserId());
        return true;
    }
}
