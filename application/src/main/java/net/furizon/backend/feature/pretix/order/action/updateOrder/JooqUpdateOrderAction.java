package net.furizon.backend.feature.pretix.order.action.updateOrder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

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
        command.execute(
            PostgresDSL
                .update(ORDERS)
                .set(ORDERS.ORDER_STATUS, (short) order.getOrderStatus().ordinal())
                .set(ORDERS.ORDER_SPONSORSHIP_TYPE, (short) order.getSponsorship().ordinal())
                .set(ORDERS.ORDER_EXTRA_DAYS_TYPE, (short) order.getExtraDays().ordinal())
                .set(ORDERS.ORDER_DAILY_DAYS, order.getDailyDaysBitmask())
                .set(ORDERS.ORDER_ROOM_CAPACITY, order.getRoomCapacity())
                .set(ORDERS.ORDER_HOTEL_LOCATION, order.getHotelLocation())
                .set(ORDERS.ORDER_SECRET, order.getPretixOrderSecret())
                .set(ORDERS.HAS_MEMBERSHIP, order.hasMembership())
                .set(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID, order.getAnswersMainPositionId())
                //.set(ORDERS.USER_ID, order.getOrderOwner())
                //.set(ORDERS.EVENT_ID, order.getOrderEvent())
                .set(ORDERS.ORDER_ANSWERS_JSON, serializer.serializeAsJson(order.getAllAnswers(pretixInformation)))
                .where(ORDERS.ORDER_CODE.eq(order.getCode()))
        );
    }
}
