package net.furizon.backend.feature.pretix.order.action.addAnswer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.util.OrderTransformationUtil;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAddPretixAnswerAction implements AddPretixAnswerAction {
    @Qualifier("pretixHttpClient")
    private final HttpClient pretixHttpClient;

    private final OrderTransformationUtil orderTransformation;

    @Override
    public boolean invoke(
        @NotNull final Order order,
        @NotNull final Event.OrganizerAndEventPair pair
    ) {
        final var request = HttpRequest.create()
            .method(HttpMethod.PATCH)
            .path("/organizers/{organizer}/events/{event}/orderpositions/{position}/")
            .uriVariable("organizer", pair.getOrganizer())
            .uriVariable("event", pair.getEvent())
            .uriVariable("position", String.valueOf(order.getAnswersMainPositionId()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(orderTransformation.getAnswersAsJson(order))
            .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode() == HttpStatus.OK;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while sending Pretix answer to order ", ex);
            return false;
        }
    }
}
