package net.furizon.backend.infrastructure.security.permissions.mapper;

import net.furizon.backend.infrastructure.security.permissions.dto.JooqUserHasRole;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

public class JooqUserHasRoleMapper {
    public static JooqUserHasRole map(Record record) {
        return new JooqUserHasRole(
                record.get(USER_HAS_ROLE.USER_ID),
                record.get(USER_HAS_ROLE.ROLE_ID),
                record.get(USER_HAS_ROLE.TEMP_EVENT_ID)
        );
    }
}
