package net.furizon.backend.infrastructure.security.session.mapper;

import net.furizon.backend.feature.authentication.mapper.JooqAuthenticationMapper;
import net.furizon.backend.infrastructure.security.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Sessions.SESSIONS;


public class JooqSessionMapper {
    @NotNull
    public static Session map(Record record) {
        return Session.builder()
            .id(record.get(SESSIONS.ID))
            .createdAt(record.get(SESSIONS.CREATED_AT))
            .expiresAt(record.get(SESSIONS.EXPIRES_AT))
            .authentication(JooqAuthenticationMapper.map(record))
            .build();
    }
}
