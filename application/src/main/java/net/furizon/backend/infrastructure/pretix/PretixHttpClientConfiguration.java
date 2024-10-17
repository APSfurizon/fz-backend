package net.furizon.backend.infrastructure.pretix;

import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.SimpleHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@EnableConfigurationProperties(PretixConfig.class)
public class PretixHttpClientConfiguration {
    @Bean("pretixHttpClient")
    HttpClient pretixHttpClient(PretixConfig config) {
        return new SimpleHttpClient(
            // TODO -> Setup Http Client Settings, like
            // max-connection-retries
            // max-connections
            // connection-timeout
            // etc...
            RestClient.create(),
            List.of(config)
        );
    }
}
