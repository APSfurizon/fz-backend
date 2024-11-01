package net.furizon.backend.feature.pretix.event.mapper;

import net.furizon.backend.feature.pretix.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Optional;

import static net.furizon.jooq.generated.Tables.EVENTS;

public class JooqEventMapper {
    @NotNull
    public static Event map(Record record) {
        return Event.builder()
            .slug(record.get(EVENTS.EVENT_SLUG))
            .dateTo(
                Optional.ofNullable(record.get(EVENTS.EVENT_DATE_TO))
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
                    .map(it -> {
                        //JSONObject d = new JSONObject(it); //TODO update deserialization method
                        //return d.keySet().stream().collect(Collectors.toMap(k -> k, d::getString));
                        return new HashMap<String, String>();
                    })
                    .orElse(null)
            )
            .build();
    }
}
