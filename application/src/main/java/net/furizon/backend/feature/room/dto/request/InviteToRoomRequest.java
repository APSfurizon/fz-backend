package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteToRoomRequest {
    @NotNull
    @Min(0L)
    private final long userId;

    @Nullable
    private final Long roomId;

    @Nullable
    private final Boolean force;

    @Nullable
    private final Boolean forceExit;
}
