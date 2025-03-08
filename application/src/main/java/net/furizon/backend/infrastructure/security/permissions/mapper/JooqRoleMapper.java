package net.furizon.backend.infrastructure.security.permissions.mapper;

import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ROLES;

public class JooqRoleMapper {
    public static Role map(Record record) {
        return new Role(
                record.get(ROLES.ROLE_ID),
                record.get(ROLES.DISPLAY_NAME),
                record.get(ROLES.INTERNAL_NAME),
                record.get(ROLES.SHOW_IN_NOSECOUNT)
        );
    }
}
