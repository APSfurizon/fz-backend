package net.furizon.backend.feature.pretix.objects.payment.action.manualCancelPayment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestManualCancelPaymentAction implements ManualCancelPaymentAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(@NotNull Event event, String orderCode, long paymentId) {
        log.info("Issuing manual cancellation of payment {} to order {} of event {}",
                paymentId, orderCode, event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .path("/organizers/{organizer}/events/{event}/orders/{code}/payments/{id}/cancel/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("code", orderCode)
                .uriVariable("id", String.valueOf(paymentId))
                .responseType(Void.class)
                .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode() == HttpStatus.OK;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while cancelling a payment", ex);
            return false;
        }
    }
}
