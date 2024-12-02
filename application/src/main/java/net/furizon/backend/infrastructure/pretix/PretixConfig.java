package net.furizon.backend.infrastructure.pretix;

import lombok.Data;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@ConfigurationProperties(prefix = "pretix")
public class PretixConfig implements HttpConfig {
    @NotNull
    private final Api api;

    @NotNull
    private final String defaultOrganizer;

    @NotNull
    private final String defaultEvent;

    private final int connectionTimeout;

    private final boolean enableSync;
  
    @NotNull
    private final String cacheReloadCronjob;

    @NotNull
    private final Shop shop;

    @NotNull
    @Override
    public String getBaseUrl() {
        return api.url;
    }

    @NotNull
    @Override
    public String getBasePath() {
        return api.path;
    }

    @NotNull
    @Override
    public MultiValueMap<String, String> headers() {
        return new LinkedMultiValueMap<>() {
            {
                add(HttpHeaders.HOST, shop.host); //Needed in prod, which talks locally with pretix
                add(HttpHeaders.AUTHORIZATION, "Token %s".formatted(api.key));
            }
        };
    }

    @Data
    public static class Api {
        @NotNull private final String url;
        @NotNull private final String path;
        @NotNull private final String key;
    }

    @Data
    public static class Shop {
        @NotNull private final String host;
        @NotNull private final String port;
        @NotNull private final String basePath;
        @NotNull private final String path;
        @NotNull private final String url;
    }
}
