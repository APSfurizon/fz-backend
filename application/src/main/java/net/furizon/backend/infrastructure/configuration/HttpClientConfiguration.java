package net.furizon.backend.infrastructure.configuration;

import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import net.furizon.backend.infrastructure.http.client.SimpleHttpClient;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Configuration
public class HttpClientConfiguration {
    @Bean
    public HttpClient httpClient(@Nullable final List<HttpConfig> configs) {
        return new SimpleHttpClient(
            RestClient.create(),
            configs != null
                ? configs
                : Collections.emptyList()
        );
    }
}
