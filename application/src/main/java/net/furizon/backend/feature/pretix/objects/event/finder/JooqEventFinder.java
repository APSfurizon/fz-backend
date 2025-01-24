package net.furizon.backend.feature.pretix.objects.event.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.mapper.JooqEventMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
public class JooqEventFinder implements EventFinder {
    private final SqlQuery query;

    private final JooqEventMapper mapper;

    @Override
    public @Nullable Event findEventBySlug(@NotNull String slug) {
        return query.fetchFirst(
            selectEvent()
            .where(EVENTS.EVENT_SLUG.eq(slug))
        ).mapOrNull(mapper::map);
    }

    @Override
    public @Nullable Event findEventById(long id) {
        return query.fetchFirst(
            selectEvent()
            .where(EVENTS.ID.eq(id))
        ).mapOrNull(mapper::map);
    }

    @Override
    public @NotNull List<Event> getAllEvents() {
        return query.fetch(selectEvent()).stream().map(mapper::map).toList();
    }

    private @NotNull SelectJoinStep<?> selectEvent() {
        return PostgresDSL.select(
                EVENTS.ID,
                EVENTS.EVENT_SLUG,
                EVENTS.EVENT_DATE_TO,
                EVENTS.EVENT_DATE_FROM,
                EVENTS.EVENT_IS_CURRENT,
                EVENTS.EVENT_PUBLIC_URL,
                EVENTS.EVENT_NAMES_JSON
            )
            .from(EVENTS);
    }
}
