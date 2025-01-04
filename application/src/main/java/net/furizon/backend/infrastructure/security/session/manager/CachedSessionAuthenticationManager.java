package net.furizon.backend.infrastructure.security.session.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.mapper.JooqAuthenticationMapper;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.Session;
import net.furizon.backend.infrastructure.security.session.mapper.JooqSessionMapper;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.SESSIONS;

@Slf4j
@Service
@RequiredArgsConstructor
public class CachedSessionAuthenticationManager implements SessionAuthenticationManager {
    @NotNull private final SqlQuery sqlQuery;
    @NotNull private final SqlCommand sqlCommand;

    @NotNull private final SecurityConfig securityConfig;

    @NotNull private final PasswordEncoder encoder;

    private final Cache<UUID, Pair<Session, Authentication>> sessionAuthenticationPairCache = Caffeine.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            .build();

    @Override
    public void updateSession(@NotNull UUID sessionId, @NotNull String clientIp) {
        sessionAuthenticationPairCache.invalidate(sessionId);
        final var now = OffsetDateTime.now();
        final var expireAt = now.plusSeconds(securityConfig.getSession().getExpiration().getSeconds());
        sqlCommand.execute(
            PostgresDSL.update(SESSIONS)
            .set(SESSIONS.LAST_USED_BY_IP_ADDRESS, clientIp)
            .set(SESSIONS.MODIFIED_AT, now)
            .set(SESSIONS.EXPIRES_AT, expireAt)
            .where(SESSIONS.ID.eq(sessionId))
        );
    }

    @Override
    public void deleteSession(@NotNull UUID sessionId) {
        sessionAuthenticationPairCache.invalidate(sessionId);
        sqlCommand.execute(
            PostgresDSL.deleteFrom(SESSIONS)
            .where(SESSIONS.ID.eq(sessionId))
        );
    }

    @Override
    public @NotNull UUID createSession(long userId, @NotNull String clientIp, @Nullable String userAgent) {
        final var sessionId = UuidCreator.getTimeOrderedEpoch();
        final var createdAt = OffsetDateTime.now();
        final var expiredAt = createdAt.plusSeconds(securityConfig.getSession().getExpiration().getSeconds());
        sqlCommand.execute(
            PostgresDSL.insertInto(
                SESSIONS,
                SESSIONS.ID,
                SESSIONS.USER_AGENT,
                SESSIONS.CREATED_BY_IP_ADDRESS,
                SESSIONS.LAST_USED_BY_IP_ADDRESS,
                SESSIONS.USER_ID,
                SESSIONS.CREATED_AT,
                SESSIONS.MODIFIED_AT,
                SESSIONS.EXPIRES_AT
            )
            .values(
                sessionId,
                userAgent,
                clientIp,
                clientIp,
                userId,
                createdAt,
                createdAt,
                expiredAt
            )
        );

        return sessionId;
    }

    @Override
    public void clearOldestSessions(long userId) {
        List<Session> sessions = getUserSessions(userId);
        sessions.stream()
                .sorted(Comparator.comparing(Session::getExpiresAt).reversed())
                // -1 because we are planning to insert one more session after the clean
                .skip((long) (securityConfig.getSession().getMaxAllowedSessionsSize() - 1))
                .forEach(session -> this.deleteSession(session.getId()));
    }

    @Override
    public void clearAllSession(long userId) {
        List<Session> sessions = getUserSessions(userId);
        for (Session session : sessions) {
            deleteSession(session.getId());
        }
    }

    @Override
    public @NotNull List<Session> getUserSessions(long userId) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                SESSIONS.ID,
                SESSIONS.USER_AGENT,
                SESSIONS.CREATED_AT,
                SESSIONS.MODIFIED_AT,
                SESSIONS.EXPIRES_AT
            )
            .from(SESSIONS)
            .where(SESSIONS.USER_ID.eq(userId))
            .orderBy(SESSIONS.CREATED_AT.desc())
        ).stream().map(JooqSessionMapper::map).toList();
    }

    @Override
    public int getUserSessionsCount(long userId) {
        return sqlQuery.count(
            PostgresDSL.select(SESSIONS.ID)
            .from(SESSIONS)
            .where(SESSIONS.USER_ID.eq(userId))
        );
    }

    @Override
    public @Nullable Pair<Session, Authentication> findSessionWithAuthenticationById(UUID sessionId) {
        Pair<Session, Authentication> res = sessionAuthenticationPairCache.getIfPresent(sessionId);
        if (res == null) {
            res = sqlQuery.fetchFirst(
                PostgresDSL.select(
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
                .innerJoin(AUTHENTICATIONS)
                .on(
                    AUTHENTICATIONS.USER_ID.eq(SESSIONS.USER_ID)
                    .and(SESSIONS.ID.eq(sessionId))
                    .and(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFIED.isNull())
                    .and(AUTHENTICATIONS.AUTHENTICATION_DISABLED.isFalse())
                )
            )
            .mapOrNull(record ->
                Pair.of(
                    JooqSessionMapper.map(record),
                    JooqAuthenticationMapper.map(record)
                )
            );
            if (res != null) {
                sessionAuthenticationPairCache.put(sessionId, res);
            }
        }
        return res;

    }

    @Override
    public void createAuthentication(long userId, @NotNull String email, @NotNull String password) {
        sqlCommand.execute(
            PostgresDSL.insertInto(
                AUTHENTICATIONS,
                AUTHENTICATIONS.USER_ID,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD
            )
            .values(
                userId,
                email,
                encoder.encode(securityConfig.getPasswordSalt() + password)
            )
        );
    }
}
