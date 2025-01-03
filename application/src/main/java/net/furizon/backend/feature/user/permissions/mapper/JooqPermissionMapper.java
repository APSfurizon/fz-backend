package net.furizon.backend.feature.user.permissions.mapper;

import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.permissions.Permission;
import net.furizon.backend.feature.user.permissions.dto.JooqPermission;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.PERMISSION;

@Slf4j
public class JooqPermissionMapper {
    @NotNull
    public static JooqPermission map(Record record) {
        Long permValue = record.get(PERMISSION.PERMISSION_VALUE);
        Permission permission = Permission.get(permValue);

        if (permission == null) {
            log.error("Permission with id {} not found, but still present in database", permValue);
            throw new IllegalArgumentException("Permission not found");
        }

        return new JooqPermission(
                record.get(PERMISSION.ROLE_ID),
                permission
        );
    }
}
