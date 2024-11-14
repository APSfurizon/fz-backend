package net.furizon.backend.feature.pretix.objects.order.action.insertNewOrder;

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
public class JooqInsertNewOrderAction implements InsertNewOrderAction {
    private final SqlCommand command;

    private final JsonSerializer jsonSerializer;

    @Override
    public void invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    ) {
        final var userId = Optional.ofNullable(order.getOrderOwner()).map(User::getId).orElse(null);
        final var eventId = order.getOrderEvent().getId();

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
                    ORDERS.ORDER_ROOM_CAPACITY,
                    ORDERS.ORDER_HOTEL_LOCATION,
                    ORDERS.ORDER_SECRET,
                    ORDERS.HAS_MEMBERSHIP,
                    ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID,
                    ORDERS.USER_ID,
                    ORDERS.EVENT_ID,
                    ORDERS.ORDER_ANSWERS_JSON
                )
                .values(
                    order.getId(),
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
                    userId,
                    eventId,
                    jsonSerializer.serializeAsJson(order.getAllAnswers(pretixInformation))
                )
        );
    }
}
