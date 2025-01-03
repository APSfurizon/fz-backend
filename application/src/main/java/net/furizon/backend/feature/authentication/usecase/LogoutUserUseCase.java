package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.session.action.deleteSession.DeleteSessionAction;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LogoutUserUseCase implements UseCase<UUID, Boolean> {
    private final DeleteSessionAction deleteSessionAction;

    @Transactional
    @Override
    public @NotNull Boolean executor(@NotNull UUID sessionId) {
        deleteSessionAction.invoke(sessionId);
        return true;
    }
}
