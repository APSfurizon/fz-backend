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
    private String protocol;

    private String hostName;

    private long port;

    private String apiKey;

    private String organizer;

    private boolean runHealthcheck;

    private String currentEvent;

    /* Connection */

    private int maxConnectionRetries;

    private int maxConnections;

    private int connectionTimeout;

    /* Profile pic */

    private long maxPropicFileSizeBytes;

    private long maxPropicWidth;

    private long minPropicWidth;

    private long maxPropicHeight;

    private long minPropicHeight;

    @NotNull
    @Override
    public String getBaseUrl() {
        return "%s://%s:%d".formatted(protocol, hostName, port);
    }

    @NotNull
    @Override
    public String getBasePath() {
        return "/api/v1/";
    }

    @NotNull
    @Override
    public MultiValueMap<String, String> headers() {
        return new LinkedMultiValueMap<>() {
            {
                add(HttpHeaders.AUTHORIZATION, "Token %s".formatted(apiKey));
                add(HttpHeaders.HOST, hostName);
            }
        };
    }
}
