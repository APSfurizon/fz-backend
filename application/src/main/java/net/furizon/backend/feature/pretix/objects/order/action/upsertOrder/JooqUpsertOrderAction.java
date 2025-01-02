package net.furizon.backend.feature.pretix.objects.order.action.upsertOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpsertOrderAction implements UpsertOrderAction {
    @NotNull private final SqlCommand command;

    @NotNull private final UserFinder userFinder;

    @NotNull private final JsonSerializer jsonSerializer;

    @Override
    public void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        String code = order.getCode();
        short orderStatus = (short) order.getOrderStatus().ordinal();
        short sponsorship = (short) order.getSponsorship().ordinal();
        short extraDays = (short) order.getExtraDays().ordinal();
        long dailyDaysBitmask = order.getDailyDaysBitmask();
        Long pretixRoomItemId = order.getPretixRoomItemId();
        short roomCapacity = order.getRoomCapacity();
        String hotelInternalName = order.getHotelInternalName();
        String orderSecret = order.getPretixOrderSecret();
        boolean membership = order.hasMembership();
        long ticketPositionId = order.getTicketPositionId();
        Long roomPositionId = order.getRoomPositionId();
        Long earlyPositionId = order.getEarlyPositionId();
        Long latePositionId = order.getLatePositionId();
        Long userId = order.getOrderOwner() == null ? null : order.getOrderOwner().getId();
        long eventId = order.getOrderEvent().getId();
        var answers = jsonSerializer.serializeAsJson(order.getAllAnswers(pretixInformation));

        if (userId != null) {
            var r = userFinder.getMailDataForUser(userId);
            if (r == null) {
                log.warn("[PRETIX] User with id {} not found while upserting order in DB. Setting it to null", userId);
                order.setOrderOwnerUserId(null);
                userId = null;
            }
        }

        command.execute(
            PostgresDSL
                .insertInto(
                    ORDERS,
                    ORDERS.ID,
                    ORDERS.ORDER_CODE,
                    ORDERS.ORDER_STATUS,
                    ORDERS.ORDER_SPONSORSHIP_TYPE,
                    ORDERS.ORDER_EXTRA_DAYS_TYPE,
                    ORDERS.ORDER_DAILY_DAYS,
                    ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                    ORDERS.ORDER_ROOM_CAPACITY,
                    ORDERS.ORDER_HOTEL_INTERNAL_NAME,
                    ORDERS.ORDER_SECRET,
                    ORDERS.HAS_MEMBERSHIP,
                    ORDERS.ORDER_TICKET_POSITION_ID,
                    ORDERS.ORDER_ROOM_POSITION_ID,
                    ORDERS.ORDER_EARLY_POSITION_ID,
                    ORDERS.ORDER_LATE_POSITION_ID,
                    ORDERS.USER_ID,
                    ORDERS.EVENT_ID,
                    ORDERS.ORDER_ANSWERS_JSON
                )
                .values(
                    order.getId(),
                    code,
                    orderStatus,
                    sponsorship,
                    extraDays,
                    dailyDaysBitmask,
                    pretixRoomItemId,
                    roomCapacity,
                    hotelInternalName,
                    orderSecret,
                    membership,
                    ticketPositionId,
                    roomPositionId,
                    earlyPositionId,
                    latePositionId,
                    userId,
                    eventId,
                    answers
                )
                .onConflict(ORDERS.ID)
                .doUpdate()
                .set(ORDERS.ORDER_STATUS, orderStatus)
                .set(ORDERS.ORDER_SPONSORSHIP_TYPE, sponsorship)
                .set(ORDERS.ORDER_EXTRA_DAYS_TYPE, extraDays)
                .set(ORDERS.ORDER_DAILY_DAYS, dailyDaysBitmask)
                .set(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID, pretixRoomItemId)
                .set(ORDERS.ORDER_ROOM_CAPACITY, roomCapacity)
                .set(ORDERS.ORDER_HOTEL_INTERNAL_NAME, hotelInternalName)
                .set(ORDERS.ORDER_SECRET, orderSecret)
                .set(ORDERS.HAS_MEMBERSHIP, membership)
                .set(ORDERS.ORDER_TICKET_POSITION_ID, ticketPositionId)
                .set(ORDERS.ORDER_ROOM_POSITION_ID, roomPositionId)
                .set(ORDERS.ORDER_EARLY_POSITION_ID, earlyPositionId)
                .set(ORDERS.ORDER_LATE_POSITION_ID, latePositionId)
                .set(ORDERS.USER_ID, userId)
                .set(ORDERS.EVENT_ID, eventId)
                .set(ORDERS.ORDER_ANSWERS_JSON, answers)
        );
    }
}
