package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.infrastructure.security.session.finder.SessionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetUserSessionsUseCase implements UseCase<Long, List<UserSession>> {
    private final SessionFinder sessionFinder;

    @Override
    public @NotNull List<UserSession> executor(@NotNull Long userId) {
        return sessionFinder.getUserSessions(userId)
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
    }
}
