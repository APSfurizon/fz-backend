package net.furizon.backend.feature.roles.mapper;

import net.furizon.backend.feature.roles.dto.ListedRoleResponse;
import org.jooq.Field;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ROLES;


public class ListedRoleMapper {
    public static ListedRoleResponse map(
            Record record,
            Field<Long> permissionCount,
            Field<Long> tempUsers,
            Field<Long> permanentUsers
    ) {
        return ListedRoleResponse.builder()
                .roleId(record.get(ROLES.ROLE_ID))
                .roleInternalName(record.get(ROLES.INTERNAL_NAME))
                .roleDisplayName(record.get(ROLES.DISPLAY_NAME))
                .showInNosecount(record.get(ROLES.SHOW_IN_NOSECOUNT))
                .permanentUsersNumber(record.get(permanentUsers))
                .temporaryUsersNumber(record.get(tempUsers))
                .permissionsNumber(record.get(permissionCount))
            .build();
    }
}
