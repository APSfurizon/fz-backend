package net.furizon.backend.feature.roles.action.addPermissoins;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.furizon.jooq.generated.Tables.PERMISSION;

@Component
@RequiredArgsConstructor
public class JooqAddPermissionAction implements AddPermissionsAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long roleId, @NotNull Set<Permission> permissions) {
        var q = PostgresDSL.insertInto(
                PERMISSION,
                PERMISSION.ROLE_ID,
                PERMISSION.PERMISSION_VALUE
        );
        for (Permission permission : permissions) {
            q = q.values(roleId, permission.getValue());
        }
        return command.execute(q) > 0;
    }
}
