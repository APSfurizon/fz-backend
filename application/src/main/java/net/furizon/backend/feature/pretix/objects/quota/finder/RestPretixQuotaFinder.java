package net.furizon.backend.feature.pretix.objects.quota.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixQuotaFinder implements PretixQuotaFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixQuota>> pretixPagedQuestion =
            new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<PretixQuota> getPagedQuotas(
            @NotNull String organizer,
            @NotNull String event,
            int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixQuota>>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/quotas/")
                .queryParam("page", String.valueOf(page))
                .uriVariable("organizer", organizer)
                .uriVariable("event", event)
                .responseParameterizedType(pretixPagedQuestion)
                .build();

        try {
            return Optional
                    .ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody())
                    .orElse(PretixPaging.empty());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }

    @Override
    public @NotNull Optional<PretixQuotaAvailability> getAvailability(@NotNull Event event, long quotaId) {
        var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<PretixQuotaAvailability>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/quotas/{quota-id}/availability/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("quota-id", String.valueOf(quotaId))
                .responseType(PretixQuotaAvailability.class)
                .build();

        try {
            return Optional.ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
