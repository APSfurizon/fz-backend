package net.furizon.backend.feature.membership.action.markInfoAsUpdated;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

@Component
@RequiredArgsConstructor
public class JooqMarkInfoAsUpdated implements MarkPersonalUserInformationAsUpdated {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public void invoke(long userId, @NotNull Event event) {
        sqlCommand.execute(
                PostgresDSL
                        .update(MEMBERSHIP_INFO)
                        .set(MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID, event.getId())
                        .where(MEMBERSHIP_INFO.USER_ID.eq(userId))
        );
    }
}
