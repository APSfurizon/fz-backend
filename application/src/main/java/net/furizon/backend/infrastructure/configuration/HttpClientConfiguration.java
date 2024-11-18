package net.furizon.backend.infrastructure.configuration;

import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import net.furizon.backend.infrastructure.http.client.SimpleHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class HttpClientConfiguration {
    @Bean
    public HttpClient httpClient(
        @NotNull final HttpClientBuilder httpClientBuilder,
        @Nullable final List<HttpConfig> configs
    ) {
        return new SimpleHttpClient(
            RestClient.builder()
                .requestFactory(
                    new HttpComponentsClientHttpRequestFactory(
                        httpClientBuilder.build()
                    )
                )
                .build(),
            configs != null
                ? configs
                : Collections.emptyList()
        );
    }
}
