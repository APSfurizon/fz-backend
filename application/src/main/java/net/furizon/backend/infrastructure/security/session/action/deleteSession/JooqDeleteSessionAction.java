package net.furizon.backend.infrastructure.security.session.action.deleteSession;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqDeleteSessionAction implements DeleteSessionAction {
    private final SqlCommand sqlCommand;

    @Override
    public void invoke(@NotNull UUID sessionId) {
        sqlCommand.execute(
            PostgresDSL
                .deleteFrom(SESSIONS)
                .where(SESSIONS.ID.eq(sessionId))
        );
    }
}
