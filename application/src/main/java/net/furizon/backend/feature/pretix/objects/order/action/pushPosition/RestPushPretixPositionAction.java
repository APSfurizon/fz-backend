package net.furizon.backend.feature.pretix.objects.order.action.pushPosition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPushPretixPositionAction implements PushPretixPositionAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull private final UpdatePretixPositionAction updatePretixPositionAction;

    @Override
    @Nullable
    public PretixPosition invoke(@NotNull Event event, @NotNull PushPretixPositionRequest position) {
        return invoke(event, position, false, null, null);
    }

    @Override
    public @Nullable PretixPosition invoke(@NotNull Event event, @NotNull PushPretixPositionRequest position,
           boolean createTempPositionFirst, @Nullable PretixInformation pretixInformation, @Nullable Long itemPrice) {
        if (createTempPositionFirst) {
            if (pretixInformation == null || itemPrice == null) {
                log.error("PushPretixPosition invoke called with createTempPositionFirst == true,"
                        + "but pretixInformation or itemPrice are null");
                return null;
            }

            // If the item we're adding is not an addon, pretix will refuse to add it as addon to another item.
            // However, it won't complain if we update an already existing addon.
            // What we do here is push a temp addon to the item, and then swap its item id with the item we actually
            //  want to push.
            // The temp item must be an addon to the original item, however by placing it in an addon category and
            //  by keeping it hidden (not visible) except for vouchers, the category won't appear at all
            //  in the add-on choice for the users
            PushPretixPositionRequest req = position.toBuilder().build();
            var itemType = req.getAddonTo() == null ? CacheItemTypes.TEMP_ITEM : CacheItemTypes.TEMP_ADDON;
            long tempItem = (long) pretixInformation.getIdsForItemType(itemType).toArray()[0];
            long originalAddonItemId = req.getItem();
            req.setItem(tempItem);
            req.setPrice(0L);
            PretixPosition tempPosition = push(event, req);
            if (tempPosition == null) {
                log.error("PushPretixPosition failed while generating a temp position id: {}", tempItem);
                return null;
            }

            long tempPosId = tempPosition.getPositionId();
            return updatePretixPositionAction.invoke(event, tempPosId, new UpdatePretixPositionRequest(
                    position.getOrderCode(),
                    originalAddonItemId,
                    itemPrice
            ));

        } else {
            return push(event, position);
        }
    }

    @Nullable
    private PretixPosition push(@NotNull Event event, @NotNull PushPretixPositionRequest position) {
        log.info("Pushing a new position ({}) to order {} on event {}",
                position.getItem(), position.getOrderCode(), event);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<PretixPosition>create()
                .method(HttpMethod.POST)
                .path("/organizers/{organizer}/events/{event}/orderpositions/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(position)
                .responseType(PretixPosition.class)
                .build();

        //TODO: Update invoice
        try {
            var req = pretixHttpClient.send(PretixConfig.class, request);
            return req.getStatusCode().is2xxSuccessful() ? req.getBody() : null;
        } catch (final HttpClientErrorException ex) {
            log.error("Error while pushing a new position to an order: {}", ex.getResponseBodyAsString());
            return null;
        }

    }
}
