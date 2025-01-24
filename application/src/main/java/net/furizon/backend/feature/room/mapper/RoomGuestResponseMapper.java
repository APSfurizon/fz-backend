package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ORDERS;

public class RoomGuestResponseMapper {
    public static RoomGuestResponse map(Record record) {
        Short status = record.get(ORDERS.ORDER_STATUS);
        return new RoomGuestResponse(
            RoomGuestMapper.map(record),
            JooqUserDisplayMapper.map(record),
            status == null ? OrderStatus.CANCELED : OrderStatus.values()[status]
        );
    }
}
