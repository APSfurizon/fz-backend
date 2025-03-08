package net.furizon.backend.feature.roles.action.deleteRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ROLES;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteRoleAction implements DeleteRoleAction {
    @NotNull private final SqlCommand command;

    @Override
    public boolean invoke(long roleId) {
        log.info("Deleting role {}", roleId);
        return command.execute(
                PostgresDSL.deleteFrom(ROLES)
                .where(ROLES.ROLE_ID.eq(roleId))
        ) > 0;
    }
}
