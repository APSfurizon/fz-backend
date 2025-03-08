package net.furizon.backend.feature.roles.action.removeUsers;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqRemoveUsersFromRoleAction implements RemoveUsersFromRoleAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long roleId, @NotNull Set<Long> userIds) {
        return command.execute(
            PostgresDSL.deleteFrom(USER_HAS_ROLE)
            .where(
                USER_HAS_ROLE.ROLE_ID.eq(roleId)
                .and(USER_HAS_ROLE.USER_ID.in(userIds))
            )
        ) > 0;
    }
}
