package net.furizon.backend.feature.pretix.objects.order.action.convertTicketOnlyOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class PretixConvertTicketOnlyOrder implements ConvertTicketOnlyOrderAction {

    @NotNull private final UpdatePretixPositionAction updatePretixPositionAction;
    @NotNull private final PushPretixPositionAction pushPretixPositionAction;
    @NotNull private final PretixPositionFinder pretixPositionFinder;
    @NotNull private final PretixOrderFinder pretixOrderFinder;

    @Override
    public boolean invoke(@NotNull Order order,
                          @NotNull PretixInformation pretixInformation,
                          @Nullable UpdateOrderInDb updateOrderInDb) {
        if (order.getRoomPositionId() != null) {
            log.error("Order {} already has a room position", order.getCode());
            return true;
        }
        Event event = pretixInformation.getCurrentEvent();
        var pair = event.getOrganizerAndEventPair();
        long noRoomItemId = (long) pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];

        try {
            OrderController.suspendWebhook();
            var p = pretixPositionFinder.fetchPositionById(event, order.getTicketPositionId());
            if (p.isEmpty()) {
                log.error("Position {} of order {} not found on pretix", order.getTicketPositionId(), order.getCode());
                return false;
            }
            PretixPosition pretixPosition = p.get();

            log.info("Converting 'ticket only' order {} to room + ticket", order.getCode());
            PushPretixPositionRequest pushReq = PushPretixPositionRequest.builder()
                    .orderCode(order.getCode())
                    .addonTo(order.getTicketPositionPosid())
                    .item(pretixPosition.getItemId())
                    .variation(pretixPosition.getVariationId())
                    .subevent(pretixPosition.getSubevent())
                    .seat(pretixPosition.getSeat())
                    .price(pretixPosition.getPrice())
                    .email(pretixPosition.getEmail())
                    .name(pretixPosition.getNameParts() == null ? pretixPosition.getName() : null)
                    .nameParts(pretixPosition.getNameParts())
                    .company(pretixPosition.getCompany())
                    .street(pretixPosition.getStreet())
                    .zipcode(pretixPosition.getZipcode())
                    .city(pretixPosition.getCity())
                    .country(pretixPosition.getCountry())
                    .state(pretixPosition.getState())
                    .answers(pretixPosition.getAnswers())
                    .validFrom(pretixPosition.getValidFrom())
                    .validUntil(pretixPosition.getValidUntil())
                    .build();

            pushPretixPositionAction.invoke(
                    event,
                    pushReq,
                    true,
                    pretixInformation,
                    PretixGenericUtils.fromStrPriceToLong(pretixPosition.getPrice())
            );

            updatePretixPositionAction.invoke(event, order.getTicketPositionId(), new UpdatePretixPositionRequest(
                    order.getCode(),
                    noRoomItemId,
                    PretixGenericUtils.fromPriceToString(0L, '.'),
                    Collections.emptyList()
            ));

            if (updateOrderInDb != null) {
                var o = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), order.getCode());
                if (o.isEmpty()) {
                    log.error("Order {} not found on pretix after converting it to room + ticket bundle",
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
}
