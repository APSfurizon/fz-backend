package net.furizon.backend.infrastructure.security.session.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.mapper.JooqAuthenticationMapper;
import net.furizon.backend.infrastructure.security.session.Session;
import net.furizon.backend.infrastructure.security.session.mapper.JooqSessionMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqSessionFinder implements SessionFinder {
    private final SqlQuery sqlQuery;

    @Override
    public @NotNull List<Session> getUserSessions(long userId) {
        return sqlQuery
            .fetch(
                PostgresDSL
                    .select(
                        SESSIONS.ID,
                        SESSIONS.USER_AGENT,
                        SESSIONS.CREATED_AT,
                        SESSIONS.MODIFIED_AT,
                        SESSIONS.EXPIRES_AT
                    )
                    .from(SESSIONS)
                    .where(SESSIONS.USER_ID.eq(userId))
                    .orderBy(SESSIONS.CREATED_AT.desc())
            )
            .stream()
            .map(JooqSessionMapper::map)
            .toList();
    }

    @Override
    public int getUserSessionsCount(long userId) {
        return sqlQuery.count(
            PostgresDSL
                .select(SESSIONS.ID)
                .from(SESSIONS)
                .where(SESSIONS.USER_ID.eq(userId))
        );
    }

    @Override
    public @Nullable Pair<Session, Authentication> findSessionWithAuthenticationById(UUID sessionId) {
        return sqlQuery
            .fetchFirst(
                PostgresDSL
                    .select(
                        SESSIONS.ID,
                        SESSIONS.USER_AGENT,
                        SESSIONS.CREATED_AT,
                        SESSIONS.MODIFIED_AT,
                        SESSIONS.EXPIRES_AT,
                        AUTHENTICATIONS.AUTHENTICATION_ID,
                        AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                        AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFIED,
                        AUTHENTICATIONS.AUTHENTICATION_2FA_ENABLED,
                        AUTHENTICATIONS.AUTHENTICATION_DISABLED,
                        AUTHENTICATIONS.AUTHENTICATION_FROM_OAUTH,
                        AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD,
                        AUTHENTICATIONS.USER_ID
                    )
                    .from(SESSIONS)
                    .leftOuterJoin(AUTHENTICATIONS)
                    .on(AUTHENTICATIONS.USER_ID.eq(SESSIONS.USER_ID))
                    .where(SESSIONS.ID.eq(sessionId))
            )
            .mapOrNull(record ->
                Pair.of(
                    JooqSessionMapper.map(record),
                    JooqAuthenticationMapper.map(record)
                )
            );
    }
}
