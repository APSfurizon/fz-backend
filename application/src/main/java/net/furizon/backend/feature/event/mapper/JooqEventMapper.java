package net.furizon.backend.feature.event.mapper;

import net.furizon.backend.feature.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.EVENTS;

public class JooqEventMapper {
    @NotNull
    public static Event map(Record record) {
        return Event.builder()
            .slug(record.get(EVENTS.EVENT_SLUG))
            .dateEnd(
                Optional.ofNullable(record.get(EVENTS.EVENT_DATE_END))
                    .map(OffsetDateTime::parse)
                    .orElse(null)
            )
            .dateFrom(
                Optional.ofNullable(record.get(EVENTS.EVENT_DATE_FROM))
                    .map(OffsetDateTime::parse)
                    .orElse(null)
            )
            .isCurrent(record.get(EVENTS.EVENT_IS_CURRENT))
            .publicUrl(record.get(EVENTS.EVENT_PUBLIC_URL))
            .eventNames(
                Optional.ofNullable(record.get(EVENTS.EVENT_NAMES))
                    .map(it ->
                        Arrays.stream(it.split(",")).collect(Collectors.toSet())
                    )
                    .orElse(null)
            )
            .build();
    }
}
