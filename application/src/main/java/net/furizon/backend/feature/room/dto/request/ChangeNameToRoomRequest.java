package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeNameToRoomRequest {
    @Nullable
    private final Long roomId;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 254)
    private final String name;
}
