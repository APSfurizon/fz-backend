package net.furizon.backend.utils.configs;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.furizon.backend.db.entities.pretix.Event;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@PropertySource(value = "file:config.properties")
@ConfigurationProperties(prefix = "pretix")
@NoArgsConstructor
public class PretixConfig {
    @Getter
    private String protocol;

    @Getter
    private String hostName;

    @Getter
    private long port;

    @Getter
    private String apiKey;

    @Setter
    @Getter
    private String organizer;

    @Getter
    @Setter
    private boolean runHealthcheck;

    @Getter
    private String currentEvent;

    /* Connection */

    @Getter
    @Setter
    private int maxConnectionRetries;

    @Getter
    @Setter
    private int maxConnections;

    @Getter
    @Setter
    private int connectionTimeout;

    /* Profile pic */

    @Getter
    @Setter
    private long maxPropicFileSizeBytes;

    @Getter
    @Setter
    private long maxPropicWidth;

    @Getter
    @Setter
    private long minPropicWidth;

    @Getter
    @Setter
    private long maxPropicHeight;

    @Getter
    @Setter
    private long minPropicHeight;

    /******************
     * Generated data *
     ******************/

    @Setter
    private Event currentEventObj;

    @Setter
    private Map<String, String> connectionHeaders = null;

    @Setter
    private String baseUrl = null;

    public void setHostName(String hostName) {
        this.hostName = hostName;
        if (connectionHeaders == null) {
            getConnectionHeaders(); //This will create them
        }
        connectionHeaders.put("Host", hostName);
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        if (connectionHeaders == null) {
            getConnectionHeaders(); //This will create them
        }
        connectionHeaders.put("Authorization", "Token " + apiKey);
    }

    public Map<String, String> getConnectionHeaders() {
        if (connectionHeaders == null) {
            connectionHeaders = new HashMap<>();
            connectionHeaders.put("Host", hostName);
            connectionHeaders.put("Authorization", "Token " + apiKey);
        }
        return connectionHeaders;
    }

    public void setEndpointUrl(String endpointUrl) {
        //TODO: Check if setters are automatically called when data is loaded from db
        if (!endpointUrl.endsWith("/")) {
            endpointUrl += "/";
        }
        baseUrl = endpointUrl + "api/v1/";
    }

    public void setCurrentEvent(String currentEvent) {
        this.currentEvent = currentEvent;
        this.currentEventObj = null;
    }

    public Event getCurrentEventObj() {
        if (currentEventObj == null) {
            //TODO: Load from db
        }
        return currentEventObj;
    }

    public String getEventUrl() {
        return getBaseUrl() + "organizers/" + organizer + "/events/" + currentEvent;
    }

    public String getBaseUrl() {
        baseUrl = getProtocol() + "://" + getHostName() + ":" + getPort() + "/api/v1/";
        return baseUrl;
    }
}
