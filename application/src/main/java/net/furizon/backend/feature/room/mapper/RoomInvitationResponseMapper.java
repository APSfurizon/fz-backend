package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

public class RoomInvitationResponseMapper {
    public static RoomInvitationResponse map(Record record, @NotNull PretixInformation pretixInformation) {
        return new RoomInvitationResponse(
            JooqRoomInfoMapper.map(record, pretixInformation),
            RoomGuestMapper.map(record)
        );
    }
}
