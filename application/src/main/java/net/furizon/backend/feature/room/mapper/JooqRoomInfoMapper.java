package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOMS;

public class JooqRoomInfoMapper {
    @NotNull
    public static RoomInfo map(@NotNull Record record,
                               @NotNull PretixInformation pretixInformation,
                               @NotNull RoomLogic roomLogic,
                               long userId, long eventId) {
        ExtraDays extraDays = roomLogic.getExtraDaysForUser(userId, eventId);
        return RoomInfo.builder()
                .roomId(record.get(ROOMS.ROOM_ID))
                .roomName(record.get(ROOMS.ROOM_NAME))
                .roomOwner(JooqUserDisplayMapper.map(record))
                .confirmed(record.get(ROOMS.ROOM_CONFIRMED))
                .roomData(JooqRoomDataMapper.map(record, pretixInformation))
                .showInNosecount(record.get(ROOMS.SHOW_IN_NOSECOUNT))
                .extraDays(extraDays == null ? ExtraDays.NONE : extraDays)
                .eventId(record.get(ORDERS.EVENT_ID))
            .build();
    }
}
