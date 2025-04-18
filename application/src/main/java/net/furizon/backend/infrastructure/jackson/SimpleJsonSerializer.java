package net.furizon.backend.infrastructure.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.JSON;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimpleJsonSerializer implements JsonSerializer {
    private final ObjectMapper objectMapper;

    @Override
    public @NotNull String serializeAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull JSON serializeAsJson(Object object) {
        return JSON.valueOf(serializeAsString(object));
    }
}
