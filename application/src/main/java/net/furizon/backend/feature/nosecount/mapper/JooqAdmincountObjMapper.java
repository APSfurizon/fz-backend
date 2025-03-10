package net.furizon.backend.feature.nosecount.mapper;

import net.furizon.backend.feature.nosecount.dto.JooqAdmincountObj;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ROLES;

public class JooqAdmincountObjMapper {
    public static JooqAdmincountObj map(Record record) {
        return JooqAdmincountObj.builder()
                .roleId(record.get(ROLES.ROLE_ID))
                .roleInternalName(record.get(ROLES.INTERNAL_NAME))
                .roleDisplayName(record.get(ROLES.DISPLAY_NAME))
                .user(JooqUserDisplayMapper.map(record, false))
            .build();
    }
}
