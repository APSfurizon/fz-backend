package net.furizon.backend.feature.pretix.objects.order.finder.pretix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
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
public class RestPretixOrderFinder implements PretixOrderFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixOrder>> pretixPagedOrder =
        new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @NotNull PretixPaging<PretixOrder> getPagedOrders(
        @NotNull final String organizer,
        @NotNull final String event,
        int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixOrder>>create()
            .method(HttpMethod.GET)
            .path("/organizers/{organizer}/events/{event}/orders/")
            .queryParam("page", String.valueOf(page))
            .uriVariable("organizer", organizer)
            .uriVariable("event", event)
            .responseParameterizedType(pretixPagedOrder)
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
    public @NotNull Optional<PretixOrder> fetchOrderByCode(
        @NotNull final String organizer,
        @NotNull final String event,
        @NotNull final String code
    ) {
        final var request = HttpRequest.<PretixOrder>create()
            .method(HttpMethod.GET)
            .path("/organizers/{organizer}/events/{event}/orders/{code}/")
            .uriVariable("organizer", organizer)
            .uriVariable("event", event)
            .uriVariable("code", code)
            .responseType(PretixOrder.class)
            .build();

        try {
            return Optional.ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
