package net.furizon.backend.feature.pretix.objects.order.action.updateOrder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqUpdateOrderAction implements UpdateOrderAction {
    private final SqlCommand command;

    private final JsonSerializer serializer;

    @Override
    public void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        final var userId = Optional.ofNullable(order.getOrderOwner()).map(User::getId).orElse(null);

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
                .set(ORDERS.ORDER_SECRET, order.getPretixOrderSecret())
                .set(ORDERS.HAS_MEMBERSHIP, order.hasMembership())
                .set(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID, order.getAnswersMainPositionId())
                .set(ORDERS.USER_ID, userId)
                .set(ORDERS.EVENT_ID, order.getOrderEvent().getId())
                .set(ORDERS.ORDER_ANSWERS_JSON, serializer.serializeAsJson(order.getAllAnswers(pretixInformation)))
                .where(ORDERS.ID.eq(order.getId()))
        );
    }
}
