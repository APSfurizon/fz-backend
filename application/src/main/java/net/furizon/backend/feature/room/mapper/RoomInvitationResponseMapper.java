package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USERS;

public class RoomInvitationResponseMapper {
    public static RoomInvitationResponse map(@NotNull Record record,
                                             @NotNull PretixInformation pretixInformation,
                                             @NotNull RoomLogic roomLogic, long eventId) {
        return new RoomInvitationResponse(
            JooqRoomInfoMapper.map(record, pretixInformation, roomLogic, record.get(USERS.USER_ID), eventId),
            RoomGuestMapper.map(record)
        );
    }
}
