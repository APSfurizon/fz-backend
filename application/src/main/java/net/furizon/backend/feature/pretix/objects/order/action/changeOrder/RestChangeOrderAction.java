package net.furizon.backend.feature.pretix.objects.order.action.changeOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.request.ChangeOrderRequest;
import net.furizon.backend.feature.pretix.objects.quota.QuotaException;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestChangeOrderAction implements ChangeOrderAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(@NotNull ChangeOrderRequest changeOrderRequest,
                          @NotNull String orderCode,
                          @NotNull Event event) {
        log.info("Changing order {} on event {}", orderCode, event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .path("/organizers/{organizer}/events/{event}/orders/{code}/change/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("code", orderCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(changeOrderRequest)
                .responseType(Void.class)
                .build();
        try {
            var resp = pretixHttpClient.send(PretixConfig.class, request, ArrayList.class);
            if (resp.isError()) {
                ArrayList<Object> rr = resp.getErrorEntity();
                String r = rr.getFirst().toString();
                if (r.toLowerCase().contains("quota")) {
                    throw new QuotaException(r);
                }
                return false;
            } else {
                return resp.getResponseEntity().getStatusCode().is2xxSuccessful();
            }
        } catch (final HttpClientErrorException ex) {
            log.error("Error while deleting a position to an order", ex);
            return false;
        }
    }
}
