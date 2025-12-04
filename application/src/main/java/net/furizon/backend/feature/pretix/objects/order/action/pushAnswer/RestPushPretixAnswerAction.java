package net.furizon.backend.feature.pretix.objects.order.action.pushAnswer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.dto.request.PushPretixAnswerRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
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
public class RestPushPretixAnswerAction implements PushPretixAnswerAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        Event event = order.getOrderEvent();
        log.info("Pushing new answers to order {} on event {}", order.getCode(), event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
            .method(HttpMethod.PATCH)
            .path("/organizers/{organizer}/events/{event}/orderpositions/{position}/")
            .uriVariable("organizer", pair.getOrganizer())
            .uriVariable("event", pair.getEvent())
            .uriVariable("position", String.valueOf(order.getTicketPositionId()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PushPretixAnswerRequest(order.getAllAnswers(pretixInformation)))
            .responseType(Void.class)
            .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode().is2xxSuccessful();
        } catch (final HttpClientErrorException ex) {
            log.error("Error while sending Pretix answer to order", ex);
            return false;
        }
    }
}
