package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.feature.user.dto.AllSessionsResponse;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetUserSessionsUseCase implements UseCase<Long, AllSessionsResponse> {
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull AllSessionsResponse executor(@NotNull Long userId) {
        AllSessionsResponse toReturn = new AllSessionsResponse();

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
        toReturn.setSessions(allSessions);
        return toReturn;
    }
}
