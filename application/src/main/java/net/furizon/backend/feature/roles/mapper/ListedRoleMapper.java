package net.furizon.backend.feature.roles.mapper;

import net.furizon.backend.feature.roles.dto.ListedRoleResponse;
import org.jooq.Field;
import org.jooq.Record;

import java.math.BigDecimal;

import static net.furizon.jooq.generated.Tables.ROLES;


public class ListedRoleMapper {
    public static ListedRoleResponse map(
            Record record,
            Field<Integer> permissionCount,
            Field<BigDecimal> tempUsers,
            Field<BigDecimal> permanentUsers
    ) {
        return ListedRoleResponse.builder()
                .roleId(record.get(ROLES.ROLE_ID))
                .roleInternalName(record.get(ROLES.INTERNAL_NAME))
                .roleDisplayName(record.get(ROLES.DISPLAY_NAME))
                .showInNosecount(record.get(ROLES.SHOW_IN_NOSECOUNT))
                .permanentUsersNumber(record.get(permanentUsers).longValue())
                .temporaryUsersNumber(record.get(tempUsers).longValue())
                .permissionsNumber(record.get(permissionCount))
            .build();
    }
}
