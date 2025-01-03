package net.furizon.backend.infrastructure.pretix.configuration;

import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.SimpleHttpClient;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Configuration
@EnableConfigurationProperties(PretixConfig.class)
public class PretixHttpClientConfiguration {
    @Bean(PRETIX_HTTP_CLIENT)
    HttpClient pretixHttpClient(
        @NotNull final HttpClientBuilder httpClientBuilder,
        @NotNull final PretixConfig config
    ) {
        final HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        factory.setConnectTimeout(config.getConnectionTimeout());
        return new SimpleHttpClient(
            RestClient.builder()
                .requestFactory(factory)
                .build(),
            List.of(config)
        );
    }
}
