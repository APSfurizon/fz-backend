package net.furizon.backend.feature.admin.mapper;

import net.furizon.backend.feature.admin.dto.JooqRoomNotFullRow;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqRoomNotFullRowMapper {
    public static JooqRoomNotFullRow map(@NotNull Record record, Field<Integer> userCount) {
        return new JooqRoomNotFullRow(
            record.get(ORDERS.USER_ID),
            record.get(userCount).shortValue(),
            record.get(ORDERS.ORDER_ROOM_CAPACITY),
            record.get(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID)
        );
    }
}
