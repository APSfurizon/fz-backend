package net.furizon.backend.feature.pretix.objects.order.action.upsertOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.JooqOptional;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record1;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpsertOrderAction implements UpsertOrderAction {
    @NotNull private final SqlCommand command;
    @NotNull private final SqlQuery sqlQuery;

    @NotNull private final UserFinder userFinder;

    @NotNull private final JsonSerializer jsonSerializer;

    @Override
    public synchronized void invoke(
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
        String roomInternalName = order.getRoomInternalName();
        String orderSecret = order.getPretixOrderSecret();
        String buyerEmail = order.getBuyerEmail();
        String buyerPhone = order.getBuyerPhone();
        String buyerUser = order.getBuyerUser();
        String buyerLocale = order.getBuyerLocale();
        String internalComment = order.getInternalComment();
        String checkinText = order.getCheckinText();
        boolean requiresAttention = order.isRequireAttention();
        boolean membership = order.hasMembership();
        long ticketPositionId = order.getTicketPositionId();
        long ticketPosid = order.getTicketPositionPosid();
        Long roomPositionId = order.getRoomPositionId();
        Long roomPosid = order.getRoomPositionPosid();
        Long earlyPositionId = order.getEarlyPositionId();
        Long latePositionId = order.getLatePositionId();
        Long userId = order.getOrderOwner() == null ? null : order.getOrderOwner().getId();
        long eventId = order.getOrderEvent().getId();
        short extraFursuits = order.getExtraFursuits();
        var answers = jsonSerializer.serializeAsJson(order.getAllAnswers(pretixInformation));

        if (userId != null) {
            var r = userFinder.getMailDataForUser(userId);
            if (r == null) {
                log.warn("[PRETIX] User with id {} not found while upserting order in DB. Setting it to null", userId);
                order.setOrderOwnerUserId(null);
                userId = null;
            }
        }

        //We don't use a sequence for the same reasons of the membership cards
        JooqOptional<Record1<Long>> r = sqlQuery.fetchFirst(
            PostgresDSL.select(
                PostgresDSL.max(ORDERS.ORDER_SERIAL_IN_EVENT)
            )
            .from(ORDERS)
            .where(ORDERS.EVENT_ID.eq(eventId))
        );
        long serial = 1L;
        if (r.isPresent()) {
            var res = r.get().get(0);
            serial = res == null ? 1L : ((long) res) + 1L;
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
                    ORDERS.ORDER_ROOM_INTERNAL_NAME,
                    ORDERS.ORDER_SECRET,
                    ORDERS.HAS_MEMBERSHIP,
                    ORDERS.ORDER_BUYER_EMAIL,
                    ORDERS.ORDER_BUYER_PHONE,
                    ORDERS.ORDER_BUYER_USER,
                    ORDERS.ORDER_BUYER_LOCALE,
                    ORDERS.ORDER_TICKET_POSITION_ID,
                    ORDERS.ORDER_TICKET_POSITION_POSITIONID,
                    ORDERS.ORDER_ROOM_POSITION_ID,
                    ORDERS.ORDER_ROOM_POSITION_POSITIONID,
                    ORDERS.ORDER_EARLY_POSITION_ID,
                    ORDERS.ORDER_LATE_POSITION_ID,
                    ORDERS.ORDER_EXTRA_FURSUITS,
                    ORDERS.ORDER_REQUIRES_ATTENTION,
                    ORDERS.ORDER_CHECKIN_TEXT,
                    ORDERS.ORDER_INTERNAL_COMMENT,
                    ORDERS.USER_ID,
                    ORDERS.EVENT_ID,
                    ORDERS.ORDER_ANSWERS_JSON,
                    ORDERS.ORDER_SERIAL_IN_EVENT
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
                    roomInternalName,
                    orderSecret,
                    membership,
                    buyerEmail,
                    buyerPhone,
                    buyerUser,
                    buyerLocale,
                    ticketPositionId,
                    ticketPosid,
                    roomPositionId,
                    roomPosid,
                    earlyPositionId,
                    latePositionId,
                    extraFursuits,
                    requiresAttention,
                    checkinText,
                    internalComment,
                    userId,
                    eventId,
                    answers,
                    serial
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
                .set(ORDERS.ORDER_ROOM_INTERNAL_NAME, roomInternalName)
                .set(ORDERS.ORDER_SECRET, orderSecret)
                .set(ORDERS.HAS_MEMBERSHIP, membership)
                .set(ORDERS.ORDER_BUYER_EMAIL, buyerEmail)
                .set(ORDERS.ORDER_BUYER_PHONE, buyerPhone)
                .set(ORDERS.ORDER_BUYER_USER, buyerUser)
                .set(ORDERS.ORDER_BUYER_LOCALE, buyerLocale)
                .set(ORDERS.ORDER_TICKET_POSITION_ID, ticketPositionId)
                .set(ORDERS.ORDER_TICKET_POSITION_POSITIONID, ticketPosid)
                .set(ORDERS.ORDER_ROOM_POSITION_ID, roomPositionId)
                .set(ORDERS.ORDER_ROOM_POSITION_POSITIONID, roomPosid)
                .set(ORDERS.ORDER_EARLY_POSITION_ID, earlyPositionId)
                .set(ORDERS.ORDER_LATE_POSITION_ID, latePositionId)
                .set(ORDERS.ORDER_EXTRA_FURSUITS, extraFursuits)
                .set(ORDERS.ORDER_REQUIRES_ATTENTION, requiresAttention)
                .set(ORDERS.ORDER_CHECKIN_TEXT, checkinText)
                .set(ORDERS.ORDER_INTERNAL_COMMENT, internalComment)
                .set(ORDERS.USER_ID, userId)
                .set(ORDERS.EVENT_ID, eventId)
                .set(ORDERS.ORDER_ANSWERS_JSON, answers)
        );
    }
}
