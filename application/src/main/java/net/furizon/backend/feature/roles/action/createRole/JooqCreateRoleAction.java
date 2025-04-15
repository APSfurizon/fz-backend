package net.furizon.backend.feature.roles.action.createRole;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ROLES;

@Component
@RequiredArgsConstructor
public class JooqCreateRoleAction implements CreateRoleAction {
    @NotNull private final SqlCommand command;


    @Override
    public long invoke(@NotNull String roleName) {
        return command.executeResult(
                PostgresDSL.insertInto(
                        ROLES,
                        ROLES.INTERNAL_NAME
                ).values(
                        roleName
                ).returning(
                        ROLES.ROLE_ID
                )
        ).getFirst().get(ROLES.ROLE_ID);
    }
}
