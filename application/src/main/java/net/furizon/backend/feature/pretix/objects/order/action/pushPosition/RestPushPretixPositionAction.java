package net.furizon.backend.feature.pretix.objects.order.action.pushPosition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPushPretixPositionAction implements PushPretixPositionAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(@NotNull Event event, @NotNull PushPretixPositionRequest position) {
        log.info("Pushing a new position ({}) to order {} on event {}", position.getItem(), position.getOrderCode(), event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .path("/organizers/{organizer}/events/{event}/orderpositions/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(position)
                .responseType(Void.class)
                .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode() == HttpStatus.OK;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while pushing a new position to an order", ex);
            return false;
        }
    }
}
