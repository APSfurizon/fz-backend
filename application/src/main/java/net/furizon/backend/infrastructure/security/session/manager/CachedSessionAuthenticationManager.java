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
import org.jooq.exception.NoDataFoundException;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.furizon.jooq.generated.Tables.*;

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
        final var now = OffsetDateTime.now();
        final var expireAt = now.plusSeconds(securityConfig.getSession().getExpiration().getSeconds());
        sqlCommand.execute(
            PostgresDSL.update(SESSIONS)
            .set(SESSIONS.LAST_USED_BY_IP_ADDRESS, clientIp)
            .set(SESSIONS.MODIFIED_AT, now)
            .set(SESSIONS.EXPIRES_AT, expireAt)
            .where(SESSIONS.ID.eq(sessionId))
        );
        sessionAuthenticationPairCache.invalidate(sessionId);
    }

    @Override
    public void deleteSession(@NotNull UUID sessionId) {
        sqlCommand.execute(
            PostgresDSL.deleteFrom(SESSIONS)
            .where(SESSIONS.ID.eq(sessionId))
        );
        sessionAuthenticationPairCache.invalidate(sessionId);
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
        final var oldestSessionIds = sessions.stream()
            .sorted(Comparator.comparing(Session::getExpiresAt).reversed())
            // -1 because we are planning to insert one more session after the clean
            .skip((long) (securityConfig.getSession().getMaxAllowedSessionsSize() - 1))
            .map(Session::getId)
            .toList();

        sqlCommand.execute(
            PostgresDSL.deleteFrom(SESSIONS)
                .where(SESSIONS.ID.in(oldestSessionIds))
        );

        sessionAuthenticationPairCache.invalidateAll(oldestSessionIds);
    }

    @Override
    public void clearAllSession(long userId) {
        final var oldSessions = sqlCommand
            .executeResult(
                PostgresDSL.deleteFrom(SESSIONS)
                    .where(SESSIONS.USER_ID.eq(userId))
                    .returning(SESSIONS.ID)
            )
            .stream()
            .map((it) -> it.get(SESSIONS.ID))
            .toList();

        sessionAuthenticationPairCache.invalidateAll(oldSessions);
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
                    AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION,
                    AUTHENTICATIONS.AUTHENTICATION_DISABLED,
                    AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD,
                    AUTHENTICATIONS.AUTHENTICATION_TOKEN,
                    AUTHENTICATIONS.USER_ID
                )
                .from(SESSIONS)
                .innerJoin(AUTHENTICATIONS)
                .on(
                    AUTHENTICATIONS.USER_ID.eq(SESSIONS.USER_ID)
                    .and(SESSIONS.ID.eq(sessionId))
                    .and(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION.isNull())
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
    public @Nullable Authentication findAuthenticationByEmail(@NotNull String email) {
        try {
            return sqlQuery.fetchSingle(
                PostgresDSL.select(
                    AUTHENTICATIONS.AUTHENTICATION_ID,
                    AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                    AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION,
                    AUTHENTICATIONS.AUTHENTICATION_DISABLED,
                    AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD,
                    AUTHENTICATIONS.AUTHENTICATION_TOKEN,
                    AUTHENTICATIONS.USER_ID
                )
                .from(AUTHENTICATIONS)
                .where(AUTHENTICATIONS.AUTHENTICATION_EMAIL.eq(email))
            ).map(JooqAuthenticationMapper::map);
        } catch (NoDataFoundException e) {
            return null;
        }
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

    @Override
    public void markEmailAsVerified(long userId) {
        sqlCommand.execute(
                PostgresDSL.update(AUTHENTICATIONS)
                .setNull(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION)
        );
        invalidateCache(userId);
    }

    @Override
    public void changePassword(long userId, @NotNull String password) {
        sqlCommand.execute(
            PostgresDSL.update(AUTHENTICATIONS)
            .set(
                AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD,
                encoder.encode(securityConfig.getPasswordSalt() + password)
            )
        );
        invalidateCache(userId);
        sqlCommand.execute(
            PostgresDSL.deleteFrom(RESET_PASSWORD_REQUESTS)
            .where(RESET_PASSWORD_REQUESTS.AUTHENTICATION_ID.eq(
                    PostgresDSL.select(AUTHENTICATIONS.AUTHENTICATION_ID)
                    .from(AUTHENTICATIONS)
                    .where(AUTHENTICATIONS.USER_ID.eq(userId))
                    .limit(1)
            ))
        );
    }

    @Override
    public int deleteUnverifiedEmailAccounts() {
        OffsetDateTime deleteFrom = OffsetDateTime.now().minusHours(securityConfig.getUnverifiedEmailExpireHours());
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(USERS)
            .where(USERS.USER_ID.in(
                PostgresDSL.select(AUTHENTICATIONS.USER_ID)
                .from(AUTHENTICATIONS)
                .where(
                    AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION.isNotNull()
                    .and(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION.lessThan(deleteFrom))
                )
            ))
        );
    }

    @Override
    public int deleteExpiredPasswordResets() {
        OffsetDateTime deleteFrom =
                OffsetDateTime.now().minusHours(securityConfig.getUnverifiedPasswordResetExpireHours());
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(RESET_PASSWORD_REQUESTS)
            .where(RESET_PASSWORD_REQUESTS.CREATED_AT.lessThan(deleteFrom))
        );
    }

    private void invalidateCache(long userId) {
        List<UUID> sessions = sqlQuery.fetch(
            PostgresDSL.select(SESSIONS.ID)
            .from(SESSIONS)
            .where(SESSIONS.USER_ID.eq(userId))
        ).stream().map(q -> q.get(SESSIONS.ID)).toList();
        sessionAuthenticationPairCache.invalidateAll(sessions);
    }
}
