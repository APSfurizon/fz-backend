package net.furizon.backend.infrastructure.security.session.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.session.Session;
import net.furizon.backend.infrastructure.security.session.mapper.JooqSessionMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.jooq.generated.Tables.SESSIONS;

@Component
@RequiredArgsConstructor
public class JooqSessionFinder implements SessionFinder {
    private final SqlQuery sqlQuery;

    @Override
    public @Nullable Session findSessionById(UUID id) {
        return sqlQuery
            .fetchFirst(
                PostgresDSL
                    .select(
                        SESSIONS.ID,
                        SESSIONS.CREATED_AT,
                        SESSIONS.EXPIRES_AT
                    )
                    .from(SESSIONS)
                    .where(SESSIONS.ID.eq(id))
            )
            .mapOrNull(JooqSessionMapper::map);
    }
}
