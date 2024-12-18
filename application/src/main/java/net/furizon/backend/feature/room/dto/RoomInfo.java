package net.furizon.backend.feature.room.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class RoomInfo {
    private final long roomId;

    @Nullable
    private final String roomName;

    private final long roomOwnerId;
    @Builder.Default
    private boolean isOwner = false;

    @Builder.Default
    @Getter(AccessLevel.NONE)
    private boolean canConfirm = false;
    @Builder.Default
    @Getter(AccessLevel.NONE)
    private boolean canUnconfirm = false;
    private final boolean confirmed;

    public boolean canBeConfirmed() {
        return canConfirm;
    }
    public boolean canBeUnconfirmed() {
        return canUnconfirm;
    }

    @NotNull
    private final RoomData roomData;

    @Builder.Default
    private boolean canInvite = false;

    @Nullable
    @Builder.Default
    private List<RoomGuestResponse> guests = null;
}
