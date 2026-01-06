package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.infrastructure.pretix.model.Board;
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
                               @NotNull Event event,
                               long userId) {
        long eventId = event.getId();
        ExtraDays extraDays = roomLogic.getExtraDaysForUser(userId, eventId);
        Board board = roomLogic.getBoardForUser(userId, eventId);
        return RoomInfo.builder()
                .roomId(record.get(ROOMS.ROOM_ID))
                .roomName(record.get(ROOMS.ROOM_NAME))
                .roomOwner(JooqUserDisplayMapper.map(record))
                .confirmed(record.get(ROOMS.ROOM_CONFIRMED))
                .roomData(JooqRoomDataMapper.map(record, pretixInformation))
                .showInNosecount(record.get(ROOMS.SHOW_IN_NOSECOUNT))
                .eventId(record.get(ORDERS.EVENT_ID))
                .checkinoutDates(event, extraDays == null ? ExtraDays.NONE : extraDays)
                .board(board == null ? Board.NONE : board)
            .build();
    }
}
