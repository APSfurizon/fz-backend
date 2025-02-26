package net.furizon.backend.infrastructure.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfiguration {
    @Bean
    @Primary
    ObjectMapper objectMapper() {
        final var mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        return mapper;
    }

    //Cannot do the classic bean approach, otherwise
    // spring will complain since csvMapper is a subclass of objectMapper
    private static CsvMapper csvMapper = null;
    public static synchronized CsvMapper csvMapper() {
        if (csvMapper == null) {
            csvMapper = new CsvMapper();
            csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            csvMapper.disable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
            csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            csvMapper.registerModule(new JavaTimeModule());
            csvMapper.findAndRegisterModules();
            csvMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return csvMapper;
    }
}
