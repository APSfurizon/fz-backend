package net.furizon.backend.feature.roles.action.deletePermissions;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.PERMISSION;

@Component
@RequiredArgsConstructor
public class JooqDeletePermissionsAction implements DeletePermissionsAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long roleId, Set<Permission> permissions) {
        Set<Long> permValues = permissions.stream().map(Permission::getValue).collect(Collectors.toSet());
        return command.execute(
            PostgresDSL.deleteFrom(PERMISSION)
            .where(
                PERMISSION.ROLE_ID.eq(roleId)
                .and(PERMISSION.PERMISSION_VALUE.in(permValues))
            )
        ) > 0;
    }
}
