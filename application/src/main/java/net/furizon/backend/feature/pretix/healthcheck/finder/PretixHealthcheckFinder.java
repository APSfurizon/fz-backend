package net.furizon.backend.feature.pretix.healthcheck.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class PretixHealthcheckFinder implements PretixHealthcheck {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull
    private final PretixConfig pretixConfig;

    @Override
    public boolean runHealthcheck() {
        if (!pretixConfig.isHealthcheckEnabled()) {
            return true;
        }

        try {
            final var request = HttpRequest.<String>create()
                    .method(HttpMethod.GET)
                    .overrideBasePath("/")
                    .path("/healthcheck/")
                    .responseType(String.class)
                    .build();
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode().is2xxSuccessful();
        } catch (final Exception ex) {
            log.error("Error while healthchecking pretix: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void waitForPretix() {
        log.info("Waiting for pretix to be online...");
        int tries;
        int max = pretixConfig.getHealthcheckRetries();
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }
        for (tries = 0; tries < max && !runHealthcheck(); tries++) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error("Error while waiting for pretix: {}", e.getMessage());
            }
        }
        if (tries >= max) {
            log.error("Pretix healthcheck failed after {} tries", tries);
            throw new RuntimeException("Pretix is offline");
        }
        log.info("Pretix is now online!");
    }
}
