package net.furizon.backend.feature.pretix.order.action.upsertOrder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.util.OrderTransformationUtil;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqUpsertOrderAction implements UpsertOrderAction {
    private final SqlCommand command;

    private final OrderTransformationUtil orderTransformationUtil;

    @Override
    public void invoke(@NotNull Order order) {
        String code = order.getCode();
        short orderStatus = (short) order.getOrderStatus().ordinal();
        short sponsorship = (short) order.getSponsorship().ordinal();
        short extraDays = (short) order.getExtraDays().ordinal();
        long dailyDaysBitmask = order.getDailyDaysBitmask();
        short roomCapacity = order.getRoomCapacity();
        String hotelLocation = order.getHotelLocation();
        String orderSecret = order.getPretixOrderSecret();
        boolean membership = order.hasMembership();
        int answersMainPositionId = order.getAnswersMainPositionId();
        //User user = order.getOrderOwner();
        //Event event = order.getOrderEvent();
        String answersJson = orderTransformationUtil.getAnswersAsJson(order);


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
                    code,
                    orderStatus,
                    sponsorship,
                    extraDays,
                    dailyDaysBitmask,
                    roomCapacity,
                    hotelLocation,
                    orderSecret,
                    membership,
                    answersMainPositionId,
                    //user,
                    //event,
                    answersJson
                )
                .onConflict(ORDERS.ORDER_CODE)
                .doUpdate()
                .set(ORDERS.ORDER_STATUS, orderStatus)
                .set(ORDERS.ORDER_SPONSORSHIP_TYPE, sponsorship)
                .set(ORDERS.ORDER_EXTRA_DAYS_TYPE, extraDays)
                .set(ORDERS.ORDER_DAILY_DAYS, dailyDaysBitmask)
                .set(ORDERS.ORDER_ROOM_CAPACITY, roomCapacity)
                .set(ORDERS.ORDER_HOTEL_LOCATION, hotelLocation)
                .set(ORDERS.ORDER_SECRET, orderSecret)
                .set(ORDERS.HAS_MEMBERSHIP, membership)
                .set(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID, answersMainPositionId)
                //.set(ORDERS.USER_ID, user)
                //.set(ORDERS.EVENT_ID, event)
                .set(ORDERS.ORDER_ANSWERS, answersJson)
        );
    }
}
