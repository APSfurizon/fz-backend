package net.furizon.backend.feature.pretix.objects.event.action.insert;

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
public class JooqInsertNewEventAction implements InsertNewEventAction {
    private final SqlCommand command;

    private final JsonSerializer jsonSerializer;

    @Override
    public long invoke(@NotNull Event event) {
        return command.executeResult(
            PostgresDSL
                .insertInto(
                    EVENTS,
                    EVENTS.EVENT_SLUG,
                    EVENTS.EVENT_DATE_TO,
                    EVENTS.EVENT_DATE_FROM,
                    EVENTS.EVENT_IS_CURRENT,
                    EVENTS.EVENT_PUBLIC_URL,
                    EVENTS.EVENT_NAMES_JSON,
                    EVENTS.EVENT_IS_LIVE,
                    EVENTS.EVENT_TEST_MODE_ENABLED,
                    EVENTS.EVENT_IS_PUBLIC
                )
                .values(
                    event.getSlug(),
                    event.getDateTo(),
                    event.getDateFrom(),
                    event.isCurrent(),
                    event.getPublicUrl(),
                    Optional
                        .ofNullable(event.getEventNames())
                        .map(jsonSerializer::serializeAsJson)
                        .orElse(null),
                    event.isLive(),
                    event.isTestModeEnabled(),
                    event.isPublic()
                ).returning(EVENTS.ID)
        ).getFirst().get(EVENTS.ID);
    }
}
