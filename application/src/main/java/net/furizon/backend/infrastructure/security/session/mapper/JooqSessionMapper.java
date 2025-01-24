package net.furizon.backend.infrastructure.security.session.mapper;

import net.furizon.backend.infrastructure.security.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.SESSIONS;


public class JooqSessionMapper {
    @NotNull
    public static Session map(Record record) {
        return Session.builder()
            .id(record.get(SESSIONS.ID))
            .userAgent(record.get(SESSIONS.USER_AGENT))
            .createdAt(record.get(SESSIONS.CREATED_AT))
            .modifiedAt(record.get(SESSIONS.MODIFIED_AT))
            .expiresAt(record.get(SESSIONS.EXPIRES_AT))
            .build();
    }
}
