package net.furizon.backend.infrastructure.configuration;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.util.TimeZone;

@Configuration
public class JacksonConfiguration {
    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(new JavaTimeModule())
                .findAndAddModules()
                .defaultTimeZone(TimeZone.getTimeZone("UTC"))
            .build();
    }

    @Bean
    CsvMapper csvMapper() {
        return CsvMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .addModule(new JavaTimeModule())
                .findAndAddModules()
                .defaultTimeZone(TimeZone.getTimeZone("UTC"))
            .build();
    }
}
