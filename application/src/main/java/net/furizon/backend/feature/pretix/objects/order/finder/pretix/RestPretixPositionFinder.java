package net.furizon.backend.feature.pretix.objects.order.finder.pretix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixPositionFinder implements PretixPositionFinder {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @NotNull Optional<PretixPosition> fetchPositionById(@NotNull Event event, final long positionId) {
        var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<PretixPosition>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/orderpositions/{position-id}/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .uriVariable("position-id", String.valueOf(positionId))
                .responseType(PretixPosition.class)
                .build();

        try {
            return Optional.ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
