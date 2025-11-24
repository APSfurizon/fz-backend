package net.furizon.backend.feature.pretix.objects.event.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static net.furizon.jooq.generated.Tables.EVENTS;

@Component
@RequiredArgsConstructor
@Slf4j
public class JooqEventMapper {
    private final TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    @NotNull
    public Event map(Record record) {
        return Event.builder()
            .id(record.get(EVENTS.ID))
            .slug(record.get(EVENTS.EVENT_SLUG))
            .dateTo(record.get(EVENTS.EVENT_DATE_TO))
            .dateFrom(record.get(EVENTS.EVENT_DATE_FROM))
            .isCurrent(record.get(EVENTS.EVENT_IS_CURRENT))
            .publicUrl(record.get(EVENTS.EVENT_PUBLIC_URL))
            .eventNames(
                Optional.ofNullable(record.get(EVENTS.EVENT_NAMES_JSON))
                    .map(it -> {
                        try {
                            return objectMapper.readValue(it.data(), typeRef);
                        } catch (JsonProcessingException e) {
                            log.error("Could not parse event names", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .orElse(null)
            )
            .isLive(record.get(EVENTS.EVENT_IS_LIVE))
            .testModeEnabled(record.get(EVENTS.EVENT_TEST_MODE_ENABLED))
            .isPublic(record.get(EVENTS.EVENT_IS_PUBLIC))
            .build();
    }
}
