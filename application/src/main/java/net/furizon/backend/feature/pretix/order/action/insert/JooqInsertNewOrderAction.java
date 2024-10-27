package net.furizon.backend.feature.pretix.order.action.insert;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqInsertNewOrderAction implements InsertNewOrderAction {
    private final SqlCommand command;

    @Override
    public void invoke(@NotNull Order order, @NotNull PretixInformation pretixInformation) {
        command.execute(
            PostgresDSL
                .insertInto(
                    ORDERS,
                        ORDERS.ORDER_CODE,
                        ORDERS.ORDER_STATUS,
                        ORDERS.ORDER_SPONSORSHIP_TYPE,
                        ORDERS.ORDER_EXTRA_DAYS_TYPE,
                        ORDERS.ORDER_DAILY_DAYS,
                        ORDERS.ORDER_ROOM_CAPACITY,
                        ORDERS.ORDER_HOTEL_LOCATION,
                        ORDERS.ORDER_SECRET,
                        ORDERS.HAS_MEMBERSHIP,
                        ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID,
                        //ORDERS.USER_ID, TODO
                        //ORDERS.EVENT_ID,
                        ORDERS.ORDER_ANSWERS
                )
                .values(
                        order.getCode(),
                        (short) order.getOrderStatus().ordinal(),
                        (short) order.getSponsorship().ordinal(),
                        (short) order.getExtraDays().ordinal(),
                        order.getDailyDaysBitmask(),
                        order.getRoomCapacity(),
                        order.getHotelLocation(),
                        order.getPretixOrderSecret(),
                        order.hasMembership(),
                        order.getAnswersMainPositionId(),
                        //order.getOrderOwner(),
                        //order.getOrderEvent(),
                        order.getAnswersJson(pretixInformation)
                )
        );
    }
}
