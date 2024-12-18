package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.RoomInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
public class RoomInfoResponse {
    @Nullable
    private final RoomInfo roomInfo;

    private final boolean canCreateRoom;

    @Nullable
    private final List<RoomInvitationResponse> invitations;
}
