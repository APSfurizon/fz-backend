package net.furizon.backend.feature.roles.mapper;

import net.furizon.backend.feature.roles.dto.UserHasRoleResponse;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

public class UserHasRoleMapper {
    public static UserHasRoleResponse map(Record record) {
        return new UserHasRoleResponse(
                record.get(USER_HAS_ROLE.TEMP_EVENT_ID) != null,
                JooqUserDisplayMapper.map(record, false)
        );
    }
}
