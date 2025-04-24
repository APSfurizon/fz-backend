package net.furizon.backend.feature.room.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class RoomInfo {
    private final long roomId;

    @Nullable
    private final String roomName;

    private final UserDisplayData roomOwner;
    @Builder.Default
    private boolean userIsOwner = false;

    @Builder.Default
    private boolean confirmationSupported = false;
    @Builder.Default
    private boolean canConfirm = false;
    @Builder.Default
    private boolean unconfirmationSupported = false;
    @Builder.Default
    private boolean canUnconfirm = false;
    private final boolean confirmed;

    private final boolean showInNosecount;

    @NotNull
    private final RoomData roomData;

    @Builder.Default
    private boolean canInvite = false;

    @NotNull
    @Builder.Default
    private final ExtraDays extraDays = ExtraDays.NONE;

    @Nullable
    @Builder.Default
    private List<RoomGuestResponse> guests = null;
}
