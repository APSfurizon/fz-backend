package net.furizon.backend.feature.nosecount.mapper;

import net.furizon.backend.feature.nosecount.dto.JooqNosecountObj;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.jooq.generated.tables.Orders;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOMS;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqNosecountObjMapper {
    public static JooqNosecountObj map(Record record, Orders roomOwnerOrder) {
        return JooqNosecountObj.builder()
                .userId(record.get(USERS.USER_ID))
                .fursonaName(record.get(USERS.USER_FURSONA_NAME))
                .userLocale(record.get(USERS.USER_LOCALE))
                .media(MediaResponseMapper.map(record))
                .sponsorship(Sponsorship.get(record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)))
                .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                .roomId(record.get(ROOMS.ROOM_ID))
                .roomName(record.get(ROOMS.ROOM_NAME))
                .roomCapacity(record.get(roomOwnerOrder.ORDER_ROOM_CAPACITY))
                .roomPretixItemId(record.get(roomOwnerOrder.ORDER_ROOM_PRETIX_ITEM_ID))
                .roomInternalName(record.get(roomOwnerOrder.ORDER_ROOM_INTERNAL_NAME))
                .hotelInternalName(record.get(roomOwnerOrder.ORDER_HOTEL_INTERNAL_NAME))
            .build();
    }
}
