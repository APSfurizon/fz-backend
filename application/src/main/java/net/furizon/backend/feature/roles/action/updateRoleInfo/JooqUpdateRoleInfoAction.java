package net.furizon.backend.feature.roles.action.updateRoleInfo;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ROLES;

@Component
@RequiredArgsConstructor
public class JooqUpdateRoleInfoAction implements UpdateRoleInformationAction {
    @NotNull private final SqlCommand command;

    @Override
    public boolean invoke(
            long roleId,
            @NotNull String roleInternalName,
            @Nullable String roleDisplayName,
            boolean showInAdminCount) {

        return command.execute(
                PostgresDSL.update(ROLES)
                .set(ROLES.INTERNAL_NAME, roleInternalName)
                .set(ROLES.DISPLAY_NAME, roleDisplayName)
                .set(ROLES.SHOW_IN_NOSECOUNT, showInAdminCount)
                .where(ROLES.ROLE_ID.eq(roleId))
        ) > 0;
    }
}
