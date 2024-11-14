package net.furizon.backend.infrastructure.pretix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "pretix")
public class PretixConfig implements HttpConfig {
    @NotNull
    private String url;

    @NotNull
    private String apiPath;

    @NotNull
    private String apiKey;

    @NotNull
    private String defaultOrganizer;

    @NotNull
    private String defaultEvent;

    @NotNull
    private String cacheReloadCronjob;

    private int connectionTimeout;

    @NotNull
    @Override
    public String getBaseUrl() {
        return url;
    }

    @NotNull
    @Override
    public String getBasePath() {
        return apiPath;
    }

    @NotNull
    @Override
    public MultiValueMap<String, String> headers() {
        return new LinkedMultiValueMap<>() {
            {
                add(HttpHeaders.AUTHORIZATION, "Token %s".formatted(apiKey));
            }
        };
    }
}
