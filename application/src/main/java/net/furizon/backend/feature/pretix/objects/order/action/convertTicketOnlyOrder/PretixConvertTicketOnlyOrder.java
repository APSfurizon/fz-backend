package net.furizon.backend.feature.pretix.objects.order.action.convertTicketOnlyOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.setAddonAsBundled.SetAddonAsBundledAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.dto.request.ConvertTicketRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.request.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.request.UpdatePretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.room.dto.request.TransferOrderRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.http.client.dto.GenericErrorResponse;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.fzBackendUtils.FzBackendUtilsErrorCodes;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Collections;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class PretixConvertTicketOnlyOrder implements ConvertTicketOnlyOrderAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;
    @NotNull
    private final PretixConfig pretixConfig;
    @NotNull
    private final PretixOrderFinder pretixOrderFinder;

    @Override
    public boolean invoke(@NotNull Order order,
                          @NotNull PretixInformation pretixInformation,
                          @Nullable UpdateOrderInDb updateOrderInDb) {
        if (order.getRoomPositionId() != null || order.getOrderStatus() == OrderStatus.PENDING || order.isDaily()) {
            log.error("Order {} already has a room position", order.getCode());
            return true;
        }
        Event event = pretixInformation.getCurrentEvent();
        var pair = event.getOrganizerAndEventPair();
        long noRoomItemId = (long) pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];

        try {
            OrderController.suspendWebhook();

            boolean result = doRequest(
                    order.getCode(),
                    order.getTicketPositionId(),
                    noRoomItemId,
                    null,
                    pair
            );

            if (!result) {
                log.error("[PRETIX_TICKET_CONVERT] Pretix request failed while converting order {}",
                        order.getCode());

                return false;
            }

            if (updateOrderInDb != null) {
                var o = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), order.getCode());
                if (o.isEmpty()) {
                    log.error("[PRETIX_TICKET_CONVERT] "
                            + "Order {} not found on pretix after converting it to room + ticket bundle",
                            order.getCode());
                    return false;
                }

                return updateOrderInDb.execute(o.get(), event, pretixInformation, false).isPresent();
            } else {
                return true;
            }
        } finally {
            OrderController.resumeWebhook();
        }
    }

    private boolean doRequest(@NotNull final String orderCode,
                             final long positionId,
                             final long newItemId,
                             @Nullable final Long newItemVariationId,
                             @NotNull Event.OrganizerAndEventPair pair) {
        log.info("[PRETIX_TICKET_CONVERT] Converting 'ticket only' order {} to room + ticket."
                + "PosId={}, ItemId={}, VarId={}", orderCode, positionId, newItemId, newItemVariationId);

        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .overrideBasePath(pretixConfig.getShop().getBasePath())
                .path("/{organizer}/{event}/fzbackendutils/api/convert-ticket-only-order/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        new ConvertTicketRequest(
                                orderCode,
                                positionId,
                                newItemId,
                                newItemVariationId
                        )
                )
                .responseType(Void.class)
                .build();

        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode().is2xxSuccessful();
        } catch (final HttpClientErrorException ex) {
            return false;
        }
    }
}
