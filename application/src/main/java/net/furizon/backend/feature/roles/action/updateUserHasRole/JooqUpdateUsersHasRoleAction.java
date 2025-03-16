package net.furizon.backend.feature.roles.action.updateUserHasRole;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.roles.dto.UpdateRoleToUserRequest;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.RowN;
import org.jooq.Table;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqUpdateUsersHasRoleAction implements UpdateUserHasRoleAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long roleId, @NotNull List<UpdateRoleToUserRequest> updateReqs, @NotNull Event event) {
        RowN[] rows  = new RowN[updateReqs.size()];
        long eventId = event.getId();
        int i = 0;
        for (UpdateRoleToUserRequest updateReq : updateReqs) {
            rows[i++] = PostgresDSL.row(Arrays.asList(
                    updateReq.getUserId(),
                    updateReq.getTempRole() ? eventId : null
            ));
        }

        final String userId = "user_id";
        final String tempEventId = "temp_event_id";
        Table<?> values = PostgresDSL.values(rows).as("vals", userId, tempEventId);

        return command.execute(
            PostgresDSL.update(USER_HAS_ROLE)
            .set(USER_HAS_ROLE.TEMP_EVENT_ID, values.field(tempEventId, Long.class))
            .from(values)
            .where(
                USER_HAS_ROLE.USER_ID.eq(values.field(userId, Long.class))
                .and(USER_HAS_ROLE.ROLE_ID.eq(roleId))
            )
        ) > 0;
    }
}
