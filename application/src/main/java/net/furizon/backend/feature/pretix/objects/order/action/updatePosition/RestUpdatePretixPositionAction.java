package net.furizon.backend.feature.pretix.objects.order.action.updatePosition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestUpdatePretixPositionAction implements UpdatePretixPositionAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public PretixPosition invoke(@NotNull Event event,
                                 long positionId,
                                 boolean checkQuota,
                                 @NotNull UpdatePretixPositionRequest position) {
        log.info("Update position {} with item {} to order {} on event {}", positionId,
                position.getItem(), position.getOrder(), event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<PretixPosition>create()
                .method(HttpMethod.PATCH)
                .path("/organizers/{organizer}/events/{event}/orderpositions/{position}/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("position", String.valueOf(positionId))
                .queryParam("check_quotas", checkQuota ? "true" : "false")
                .contentType(MediaType.APPLICATION_JSON)
                .body(position)
                .responseType(PretixPosition.class)
                .build();
        try {
            var req = pretixHttpClient.send(PretixConfig.class, request);
            return req.getStatusCode().is2xxSuccessful() ? req.getBody() : null;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while updating a position to an order", ex);
            return null;
        }
    }
}
