package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.user.mapper.JooqDisplayUserMapper;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Orders.ORDERS;

public class RoomGuestResponseMapper {
    public static RoomGuestResponse map(Record record) {
        Short status = record.get(ORDERS.ORDER_STATUS);
        return new RoomGuestResponse(
            RoomGuestMapper.map(record),
            JooqDisplayUserMapper.map(record),
            status == null ? null : OrderStatus.values()[status]
        );
    }
}
