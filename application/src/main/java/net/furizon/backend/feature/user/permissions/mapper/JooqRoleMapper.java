package net.furizon.backend.feature.user.permissions.mapper;

import net.furizon.backend.feature.user.permissions.Role;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ROLE;

public class JooqRoleMapper {
    public static Role map(Record record) {
        return new Role(
                record.get(ROLE.ROLE_ID),
                record.get(ROLE.DISPLAY_NAME),
                record.get(ROLE.INTERNAL_NAME)
        );
    }
}
