package net.furizon.backend.feature.event.action.insert;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.event.Event;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
public class JooqInsertNewEventAction implements InsertNewEventAction {
    private final SqlCommand command;

    @Override
    public void invoke(@NotNull Event event) {
        command.execute(
            PostgresDSL
                .insertInto(
                    EVENTS,
                    EVENTS.EVENT_SLUG,
                    EVENTS.EVENT_DATE_END,
                    EVENTS.EVENT_DATE_FROM,
                    EVENTS.EVENT_IS_CURRENT,
                    EVENTS.EVENT_PUBLIC_URL,
                    EVENTS.EVENT_NAMES
                )
                .values(
                    event.getSlug(),
                    Optional
                        .ofNullable(event.getDateEnd()).map(OffsetDateTime::toString)
                        .orElse(null),
                    Optional
                        .ofNullable(event.getDateFrom()).map(OffsetDateTime::toString)
                        .orElse(null),
                    event.getIsCurrent(),
                    event.getPublicUrl(),
                    Optional
                        .ofNullable(event.getEventNames()).map(it -> String.join(",", it))
                        .orElse(null)
                )
        );
    }
}
