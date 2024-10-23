package net.furizon.backend.feature.product.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.product.PretixProduct;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RestPretixProductFinder implements PretixProductFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixProduct>> pretixPagedProduct =
        new ParameterizedTypeReference<>() {};

    @Qualifier("pretixHttpClient")
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<PretixProduct> getPagedProducts(
            @NotNull String organizer,
            @NotNull String event,
            int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixProduct>>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/items")
                .queryParam("page", String.valueOf(page))
                .uriVariable("organizer", organizer)
                .uriVariable("event", event)
                .responseParameterizedType(pretixPagedProduct)
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
}
