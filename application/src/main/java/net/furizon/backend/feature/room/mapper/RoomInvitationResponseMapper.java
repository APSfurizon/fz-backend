package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USERS;

public class RoomInvitationResponseMapper {
    public static RoomInvitationResponse map(@NotNull Record record,
                                             @NotNull PretixInformation pretixInformation,
                                             @NotNull RoomLogic roomLogic,
                                             @NotNull Event event) {
        return new RoomInvitationResponse(
            JooqRoomInfoMapper.map(record, pretixInformation, roomLogic, event, record.get(USERS.USER_ID)),
            RoomGuestMapper.map(record)
        );
    }
}
