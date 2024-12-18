package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.user.mapper.JooqDisplayUserMapper;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.Rooms.ROOMS;

public class JooqRoomInfoMapper {
    @NotNull
    public static RoomInfo map(Record record, PretixInformation pretixInformation) {
        return RoomInfo.builder()
                .roomId(record.get(ROOMS.ROOM_ID))
                .roomName(record.get(ROOMS.ROOM_NAME))
                .roomOwner(JooqDisplayUserMapper.map(record))
                .confirmed(record.get(ROOMS.ROOM_CONFIRMED))
                .roomData(JooqRoomDataMapper.map(record, pretixInformation))
            .build();
    }
}
