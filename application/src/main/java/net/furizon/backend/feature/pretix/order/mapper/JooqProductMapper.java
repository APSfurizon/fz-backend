package net.furizon.backend.feature.pretix.order.mapper;

import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqProductMapper {
    @NotNull
    public static Order map(Record record, PretixInformation pi) {
        return Order.builder()
                .code(record.get(ORDERS.ORDER_CODE))
                .orderStatus(OrderStatus.values()[record.get(ORDERS.ORDER_STATUS)])
                .sponsorship(Sponsorship.values()[record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)])
                .extraDays(ExtraDays.values()[record.get(ORDERS.EXTRA_DAYS)])
                .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                .roomCapacity(record.get(ORDERS.ROOM_CAPACITY)) //TODO change after db regeneration
                .hotelLocation(record.get(ORDERS.HOTEL_LOCATION))
                .pretixOrderSecret(record.get(ORDERS.PRETIX_ORDER_SECRET))
                .hasMembership(record.get(ORDERS.HAS_MEMBERSHIP))
                .answersMainPositionId(record.get(ORDERS.ANSWERS_MAIN_POSITION_ID))
                //.orderOwner() TODO
                //.orderEvent() TODO
                .answers(record.get(ORDERS.ORDER_ANSWERS), pi)
                .build();
    }
}
