package net.furizon.backend.infrastructure.security.session.finder;

import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.infrastructure.security.session.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface SessionFinder {
    @NotNull
    List<Session> getUserSessions(long userId);

    int getUserSessionsCount(long userId);

    @Nullable
    Pair<Session, Authentication> findSessionWithAuthenticationById(UUID sessionId);
}
