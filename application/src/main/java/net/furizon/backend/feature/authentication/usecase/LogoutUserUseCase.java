package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutUserUseCase implements UseCase<UUID, Boolean> {
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @Transactional
    @Override
    public @NotNull Boolean executor(@NotNull UUID sessionId) {
        log.info("LogoutUserUseCase: sessionId: {}", sessionId);
        return sessionAuthenticationManager.deleteSession(sessionId);
    }
}
