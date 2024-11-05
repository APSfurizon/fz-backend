package net.furizon.backend.infrastructure.security.session.action.clearNewestUserSessions;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqClearNewestUserSessionsAction implements ClearNewestUserSessionsAction {
    private final SqlCommand sqlCommand;

    private final SecurityConfig config;

    @Override
    public void invoke(long userId) {
        sqlCommand.execute(
            PostgresDSL.deleteFrom(SESSIONS)
                .where(SESSIONS.USER_ID.eq(userId))
                .and(
                    SESSIONS.CREATED_AT.eq(
                        PostgresDSL
                            .select(SESSIONS.CREATED_AT)
                            .from(SESSIONS)
                            .where(SESSIONS.USER_ID.eq(userId))
                            .orderBy(SESSIONS.CREATED_AT)
                            .limit(1)
                            // -1 because we are planing to insert one more session after the clean
                            .offset(config.getSession().getMaxAllowedSessionsSize() - 1)
                    )
                )
        );
    }
}
