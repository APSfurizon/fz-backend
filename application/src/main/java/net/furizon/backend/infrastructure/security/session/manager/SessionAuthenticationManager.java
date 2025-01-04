package net.furizon.backend.infrastructure.security.session.manager;

import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.infrastructure.security.session.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface SessionAuthenticationManager {
    //Write
    void updateSession(@NotNull UUID sessionId, @NotNull String clientIp);
    void deleteSession(@NotNull UUID sessionId);
    @NotNull UUID createSession(long userId, @NotNull String clientIp, @Nullable String userAgent);
    void clearOldestSessions(long userId);

    void clearAllSession(long userId);

    //Read
    @NotNull List<Session> getUserSessions(long userId);
    int getUserSessionsCount(long userId);
    @Nullable Pair<Session, Authentication> findSessionWithAuthenticationById(UUID sessionId);

    void createAuthentication(long userId, @NotNull String email, @NotNull String password);
}
