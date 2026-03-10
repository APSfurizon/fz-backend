package net.furizon.backend.feature.roles.action.addUsers;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.requests.UpdateRoleToUserRequest;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqAddUsersToRoleAction implements AddUserToRoleAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invokeMultiple(long roleId, @NotNull List<UpdateRoleToUserRequest> users, @NotNull Event event) {
        long eventId = event.getId();
        var q = PostgresDSL.insertInto(
            USER_HAS_ROLE,
            USER_HAS_ROLE.ROLE_ID,
            USER_HAS_ROLE.USER_ID,
            USER_HAS_ROLE.TEMP_EVENT_ID
        );
        for (UpdateRoleToUserRequest user : users) {
            q = q.values(
                roleId,
                user.getUserId(),
                user.getTempRole() ? eventId : null
            );
        }
        return command.execute(q) > 0;
    }

    @Override
    public boolean invokeSingle(long roleId, @NotNull UpdateRoleToUserRequest user, @NotNull Event event) {
        return invokeSingle(roleId, user.getUserId(), user.getTempRole(), event);
    }

    @Override
    public boolean invokeSingle(long roleId, long userId, boolean isTempRole, @NotNull Event event) {
        return command.execute(
            PostgresDSL.insertInto(
                    USER_HAS_ROLE,
                    USER_HAS_ROLE.ROLE_ID,
                    USER_HAS_ROLE.USER_ID,
                    USER_HAS_ROLE.TEMP_EVENT_ID
            ).values(
                    roleId,
                    userId,
                    isTempRole ? event.getId() : null
            )
        ) > 0;
    }
}
