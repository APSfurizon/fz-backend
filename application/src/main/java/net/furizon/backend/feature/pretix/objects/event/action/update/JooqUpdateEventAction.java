package net.furizon.backend.feature.pretix.objects.event.action.update;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
public class JooqUpdateEventAction implements UpdateEventAction {
    private final SqlCommand command;

    private final JsonSerializer jsonSerializer;

    @Override
    public void invoke(@NotNull final Event event) {
        command.execute(
            PostgresDSL
                .update(EVENTS)
                .set(EVENTS.EVENT_DATE_FROM, event.getDateFrom())
                .set(EVENTS.EVENT_IS_CURRENT, event.isCurrent())
                .set(EVENTS.EVENT_PUBLIC_URL, event.getPublicUrl())
                .set(EVENTS.EVENT_NAMES_JSON, Optional
                    .ofNullable(event.getEventNames())
                    .map(jsonSerializer::serializeAsJson)
                    .orElse(null))
                .set(EVENTS.EVENT_DATE_TO, event.getDateTo())
                .where(EVENTS.ID.eq(event.getId()))
        );
    }
}
