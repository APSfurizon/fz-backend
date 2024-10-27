package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushAnswersToPretixUseCase implements UseCase<PushAnswersToPretixUseCase.Input, Boolean> {

    @Qualifier("pretixHttpClient")
    private final HttpClient pretixHttpClient;

    @Transactional
    @NotNull
    @Override
    public Boolean executor(@NotNull PushAnswersToPretixUseCase.Input input) {
        Event.OrganizerAndEventPair eventOrgPair = input.event.getOrganizerAndEventPair();

        byte[] jsonData = input.order.getAnswersJson(input.pretixInformation).getBytes();

        final var request  = HttpRequest.create()
                .method(HttpMethod.PATCH)
                .path("/organizers/{organizer}/events/{event}/orderpositions/{position}/")
                .uriVariable("organizer", eventOrgPair.getOrganizer())
                .uriVariable("event", eventOrgPair.getEvent())
                .uriVariable("position", String.valueOf(input.order.getAnswersMainPositionId()))
                .body(jsonData, MediaType.APPLICATION_JSON, jsonData.length)
                .build();
        try {
            var resp = pretixHttpClient.send(PretixConfig.class, request);
            if (resp != null) {
                return resp.getStatusCode() == HttpStatus.OK;
            }
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
        }
        return false;
    }
    public record Input(@NotNull Event event, @NotNull Order order, @NotNull PretixInformation pretixInformation) {}
}
