package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class InviteToRoomRequest {
    @NotNull
    @NotEmpty
    private final Set<Long> userIds;

    @Nullable
    private final Long roomId;

    @Nullable
    private final Boolean force;

    @Nullable
    private final Boolean forceExit;
}
