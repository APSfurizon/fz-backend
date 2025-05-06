package net.furizon.backend.infrastructure.pretix;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.OffsetDateTime;
import java.util.List;

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

    private final int healthcheckRetries;
  
    @NotNull
    private final String cacheReloadCronjob;

    @NotNull
    private final Shop shop;

    @NotNull
    private final List<String> supportedLanguages;

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
    private final Event event;

    @NotNull
    @Override
    public MultiValueMap<String, String> headers() {
        return new LinkedMultiValueMap<>() {
            {
                add(HttpHeaders.HOST, shop.host); //Needed in prod, which talks locally with pretix
                add(HttpHeaders.AUTHORIZATION, "Token %s".formatted(api.key));
                add(PretixConst.FZBACKENDUTILS_API_HEADER_NAME, api.fzbackendutilsToken);
            }
        };
    }

    @Data
    public static class Event {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Nullable private OffsetDateTime editBookingEndTime;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Nullable private OffsetDateTime publicBookingStartTime;
        private boolean includeEarlyInDailyCount;
    }

    @Data
    public static class Api {
        @NotNull private final String url;
        @NotNull private final String path;
        @NotNull private final String key;
        @NotNull private final String fzbackendutilsToken;
    }

    @Data
    public static class Shop {
        @NotNull private final String host;
        @NotNull private final String port;
        @NotNull private final String basePath;
        @NotNull private final String path;
        @NotNull private final String url;

        @NotNull
        public String getOrderUrl(@NotNull Order order) {
            return url + "order/" + order.getCode() + "/" + order.getPretixOrderSecret() + "/";
        }
    }
}
