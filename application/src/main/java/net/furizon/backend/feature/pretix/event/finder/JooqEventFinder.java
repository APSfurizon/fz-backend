package net.furizon.backend.feature.pretix.event.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.event.mapper.JooqEventMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
public class JooqEventFinder implements EventFinder {
    public final SqlQuery query;

    @Override
    public @Nullable Event findEventBySlug(@NotNull String slug) {
        return query.fetchFirst(
                PostgresDSL
                    .select(
                        EVENTS.EVENT_SLUG,
                        EVENTS.EVENT_DATE_TO,
                        EVENTS.EVENT_DATE_FROM,
                        EVENTS.EVENT_IS_CURRENT,
                        EVENTS.EVENT_PUBLIC_URL,
                        EVENTS.EVENT_NAMES
                    )
                    .from(EVENTS)
                    .where(EVENTS.EVENT_SLUG.eq(slug))
            )
            .mapOrNull(JooqEventMapper::map);
    }
}
