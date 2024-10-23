package net.furizon.backend.feature.pretix.event.action.update;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
public class JooqUpdateEventAction implements UpdateEventAction {
    private final SqlCommand command;

    @Override
    public void invoke(@NotNull Event event) {
        command.execute(
            PostgresDSL
                .update(EVENTS)
                .set(EVENTS.EVENT_DATE_FROM, Optional
                    .ofNullable(event.getDateFrom())
                    .map(OffsetDateTime::toString)
                    .orElse(null))
                .set(EVENTS.EVENT_IS_CURRENT, event.isCurrent())
                .set(EVENTS.EVENT_PUBLIC_URL, event.getPublicUrl())
                .set(EVENTS.EVENT_NAMES, Optional
                    .ofNullable(event.getEventNames())
                    .map(it -> new JSONObject(it).toString())
                    .orElse(null))
                .set(EVENTS.EVENT_DATE_TO, Optional
                    .ofNullable(event.getDateTo())
                    .map(OffsetDateTime::toString)
                    .orElse(null))
                .where(EVENTS.EVENT_SLUG.eq(event.getSlug()))
        );
    }
}
