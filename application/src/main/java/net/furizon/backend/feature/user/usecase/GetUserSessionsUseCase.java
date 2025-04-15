package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.feature.user.dto.SessionListResponse;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetUserSessionsUseCase implements UseCase<Long, SessionListResponse> {
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull SessionListResponse executor(@NotNull Long userId) {
        List<UserSession> allSessions = sessionAuthenticationManager.getUserSessions(userId)
                .stream()
                .map(session ->
                        new UserSession(
                                session.getId(),
                                session.getUserAgent(),
                                session.getCreatedAt(),
                                session.getModifiedAt()
                        )
                )
                .toList();
        return new SessionListResponse(allSessions);
    }
}
