package net.furizon.backend.feature.pretix.objects.order.action.updateOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpdateOrderAction implements UpdateOrderAction {
    @NotNull private final SqlCommand command;

    @NotNull private final UserFinder userFinder;

    @NotNull private final JsonSerializer serializer;

    @Override
    public void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        Long userId = Optional.ofNullable(order.getOrderOwner()).map(User::getId).orElse(null);
        if (userId != null) {
            var r = userFinder.getMailDataForUser(userId);
            if (r == null) {
                log.warn("[PRETIX] User with id {} not found while updating order in DB. Setting it to null", userId);
                order.setOrderOwnerUserId(null);
                userId = null;
            }
        }

        command.execute(
            PostgresDSL
                .update(ORDERS)
                .set(ORDERS.ORDER_STATUS, (short) order.getOrderStatus().ordinal())
                .set(ORDERS.ORDER_SPONSORSHIP_TYPE, (short) order.getSponsorship().ordinal())
                .set(ORDERS.ORDER_EXTRA_DAYS_TYPE, (short) order.getExtraDays().ordinal())
                .set(ORDERS.ORDER_DAILY_DAYS, order.getDailyDaysBitmask())
                .set(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID, order.getPretixRoomItemId())
                .set(ORDERS.ORDER_ROOM_CAPACITY, order.getRoomCapacity())
                .set(ORDERS.ORDER_HOTEL_INTERNAL_NAME, order.getHotelInternalName())
                .set(ORDERS.ORDER_ROOM_INTERNAL_NAME, order.getRoomInternalName())
                .set(ORDERS.ORDER_SECRET, order.getPretixOrderSecret())
                .set(ORDERS.HAS_MEMBERSHIP, order.hasMembership())
                .set(ORDERS.ORDER_BUYER_EMAIL, order.getBuyerEmail())
                .set(ORDERS.ORDER_BUYER_PHONE, order.getBuyerPhone())
                .set(ORDERS.ORDER_BUYER_USER, order.getBuyerUser())
                .set(ORDERS.ORDER_BUYER_LOCALE, order.getBuyerLocale())
                .set(ORDERS.ORDER_TICKET_POSITION_ID, order.getTicketPositionId())
                .set(ORDERS.ORDER_TICKET_POSITION_POSITIONID, order.getTicketPositionPosid())
                .set(ORDERS.ORDER_ROOM_POSITION_ID, order.getRoomPositionId())
                .set(ORDERS.ORDER_ROOM_POSITION_POSITIONID, order.getRoomPositionPosid())
                .set(ORDERS.ORDER_EARLY_POSITION_ID, order.getEarlyPositionId())
                .set(ORDERS.ORDER_LATE_POSITION_ID, order.getLatePositionId())
                .set(ORDERS.ORDER_EXTRA_FURSUITS, order.getExtraFursuits())
                .set(ORDERS.ORDER_REQUIRES_ATTENTION, order.isRequireAttention())
                .set(ORDERS.ORDER_CHECKIN_TEXT, order.getCheckinText())
                .set(ORDERS.ORDER_INTERNAL_COMMENT, order.getInternalComment())
                .set(ORDERS.USER_ID, userId)
                .set(ORDERS.EVENT_ID, order.getOrderEvent().getId())
                .set(ORDERS.ORDER_ANSWERS_JSON, serializer.serializeAsJson(order.getAllAnswers(pretixInformation)))
                .where(ORDERS.ID.eq(order.getId()))
        );
    }
}
