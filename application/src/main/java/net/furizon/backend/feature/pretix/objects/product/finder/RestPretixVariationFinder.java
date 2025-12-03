package net.furizon.backend.feature.pretix.objects.product.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.PretixProductVariation;
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

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixVariationFinder implements PretixVariationFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixProductVariation>> pretixPagedVariation =
        new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<PretixProductVariation> getPagedVariations(
            @NotNull String organizer,
            @NotNull String event,
            long itemId,
            int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixProductVariation>>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/items/{item}/variations/")
                .queryParam("page", String.valueOf(page))
                .uriVariable("organizer", organizer)
                .uriVariable("event", event)
                .uriVariable("item", String.valueOf(itemId))
                .responseParameterizedType(pretixPagedVariation)
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

    @NotNull
    @Override
    public Optional<PretixProductVariation> fetchVariationById(@NotNull Event event, long itemId, long variationId) {
        var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<PretixProductVariation>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/items/{item}/variations/{variation}/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("item", String.valueOf(itemId))
                .uriVariable("variation", String.valueOf(variationId))
                .responseType(PretixProductVariation.class)
                .build();

        try {
            return Optional.ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
