package net.furizon.backend.feature.pretix.order.mapper;

import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqOrderMapper {
    @NotNull
    public static Order map(Record record, PretixInformation pi) {
        return Order.builder()
                .code(record.get(ORDERS.ORDER_CODE))
                .orderStatus(OrderStatus.values()[record.get(ORDERS.ORDER_STATUS)])
                .sponsorship(Sponsorship.values()[record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)])
                .extraDays(ExtraDays.values()[record.get(ORDERS.ORDER_EXTRA_DAYS_TYPE)])
                .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                .roomCapacity(record.get(ORDERS.ORDER_ROOM_CAPACITY)) //TODO change after db regeneration
                .hotelLocation(record.get(ORDERS.ORDER_HOTEL_LOCATION))
                .pretixOrderSecret(record.get(ORDERS.ORDER_SECRET))
                .hasMembership(record.get(ORDERS.HAS_MEMBERSHIP))
                .answersMainPositionId(record.get(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID))
                //.orderOwner() TODO
                //.orderEvent() TODO
                .answers(record.get(ORDERS.ORDER_ANSWERS), pi)
                .build();
    }
}
