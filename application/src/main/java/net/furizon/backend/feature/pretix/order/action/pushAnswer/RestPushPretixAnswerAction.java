package net.furizon.backend.feature.pretix.order.action.pushAnswer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.dto.PushPretixAnswerRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestPushPretixAnswerAction implements PushPretixAnswerAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        final var pair = order.getOrderEvent().getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
            .method(HttpMethod.PATCH)
            .path("/organizers/{organizer}/events/{event}/orderpositions/{position}/")
            .uriVariable("organizer", pair.getOrganizer())
            .uriVariable("event", pair.getEvent())
            .uriVariable("position", String.valueOf(order.getAnswersMainPositionId()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PushPretixAnswerRequest(order.getAllAnswers(pretixInformation)))
            .responseType(Void.class)
            .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode() == HttpStatus.OK;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while sending Pretix answer to order ", ex);
            return false;
        }
    }
}
