package net.furizon.backend.feature.roles.action.addUsers;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.UpdateRoleToUserRequest;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqAddUsersToRoleAction implements AddUsersToRoleAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long roleId, @NotNull List<UpdateRoleToUserRequest> users, @NotNull Event event) {
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
}
