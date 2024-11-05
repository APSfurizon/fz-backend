package net.furizon.backend.infrastructure.security.session.action.updateSession;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqUpdateSessionAction implements UpdateSessionAction {
    private final SqlCommand sqlCommand;

    private final SecurityConfig securityConfig;

    @Override
    public void invoke(@NotNull UUID sessionId, @NotNull String clientIp) {
        final var now = OffsetDateTime.now();
        final var expireAt = now.plusSeconds(securityConfig.getSession().getExpiration().getSeconds());
        sqlCommand.execute(
            PostgresDSL
                .update(SESSIONS)
                .set(SESSIONS.LAST_USED_BY_IP_ADDRESS, clientIp)
                .set(SESSIONS.MODIFIED_AT, now)
                .set(SESSIONS.EXPIRES_AT, expireAt)
                .where(SESSIONS.ID.eq(sessionId))
        );
    }
}
