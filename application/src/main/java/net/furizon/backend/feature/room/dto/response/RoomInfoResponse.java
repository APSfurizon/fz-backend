package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomInfo;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class RoomInfoResponse {
    @Nullable
    private final RoomInfo currentRoomInfo;

    private final boolean hasOrder;
    private final boolean allowedModifications;
    private final boolean canCreateRoom;
    private final boolean buyOrUpgradeRoomSupported;
    private final boolean canBuyOrUpgradeRoom;
    private final boolean canExchange;

    @Nullable
    private final OffsetDateTime editingRoomEndTime;

    @Nullable
    private final List<RoomInvitationResponse> invitations;
}
