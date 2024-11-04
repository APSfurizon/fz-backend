package net.furizon.backend.infrastructure.pretix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pretix")
public class PretixConfig implements HttpConfig {
    @NotNull
    private final String url;

    @NotNull
    private final String apiPath;

    @NotNull
    private final String apiKey;

    @NotNull
    private final String defaultOrganizer;

    @NotNull
    private final String defaultEvent;

    private final int connectionTimeout;

    private final boolean enableSync;

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
