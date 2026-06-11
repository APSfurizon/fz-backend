package net.furizon.backend.feature.gallery;

import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.SimpleHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@EnableConfigurationProperties(GalleryConfig.class)
public class GalleryProcessorHttpClient {
    public static final String GALLERY_HTTP_CLIENT = "gallery-http-client";

    @Bean(GALLERY_HTTP_CLIENT)
    HttpClient galleryHttpClient(
            @NotNull final HttpClientBuilder httpClientBuilder,
            @NotNull final GalleryConfig config
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
