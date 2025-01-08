package net.furizon.backend.feature.pretix.objects.payment.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixPaymentFinder implements PretixPaymentFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixPayment>> pretixPagedPayment =
            new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<PretixPayment> getPagedPayments(
            @NotNull String organizer,
            @NotNull String event,
            @NotNull String orderCode,
            int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixPayment>>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/orders/{code}/payments/")
                .queryParam("page", String.valueOf(page))
                .uriVariable("organizer", organizer)
                .uriVariable("event", event)
                .uriVariable("code", orderCode)

                .responseParameterizedType(pretixPagedPayment)
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
    public List<PretixPayment> getPaymentsForOrder(
        @NotNull Event event,
        @NotNull String orderCode
    ) {
        var pair = event.getOrganizerAndEventPair();
        String e = pair.getEvent();
        String o = pair.getOrganizer();
        return PretixPagingUtil.fetchAll(
            page -> getPagedPayments(
                o, e, orderCode, page
            )
        );
    }
}
