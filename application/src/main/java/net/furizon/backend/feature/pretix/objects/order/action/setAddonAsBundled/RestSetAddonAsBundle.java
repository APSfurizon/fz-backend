package net.furizon.backend.feature.pretix.objects.order.action.setAddonAsBundled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.request.UpdatePositionBundleStatus;
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
public class RestSetAddonAsBundle implements SetAddonAsBundledAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;
    @NotNull private final PretixConfig pretixConfig;

    @Override
    public boolean invoke(long positionId, boolean isBundled, @NotNull Event event) {
        log.info("Updating bundle status of position {} to {}", positionId, isBundled);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .overrideBasePath(pretixConfig.getShop().getBasePath())
                .path("/{organizer}/{event}/fzbackendutils/api/set-item-bundle/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdatePositionBundleStatus(positionId, isBundled))
                .responseType(Void.class)
                .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode().is2xxSuccessful();
        } catch (final HttpClientErrorException ex) {
            log.error("Error updating bundle status", ex);
            return false;
        }
    }
}
