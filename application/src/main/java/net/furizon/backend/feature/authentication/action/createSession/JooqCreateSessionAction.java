package net.furizon.backend.feature.authentication.action.createSession;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqCreateSessionAction implements CreateSessionAction {
    private final SqlCommand sqlCommand;

    private final SecurityConfig securityConfig;

    @Override
    public UUID invoke(
        long userId,
        @NotNull String clientIp,
        @Nullable String userAgent
    ) {
        final var sessionId = UuidCreator.getTimeOrderedEpoch();
        final var createdAt = OffsetDateTime.now(ZoneId.of("UTC"));
        final var expiredAt = createdAt.plusSeconds(securityConfig.getSession().getExpiration().getSeconds());
        sqlCommand.execute(
            PostgresDSL
                .insertInto(
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
        return null;
    }
}
