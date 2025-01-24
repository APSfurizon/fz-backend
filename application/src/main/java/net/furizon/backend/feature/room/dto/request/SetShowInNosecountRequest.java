package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetShowInNosecountRequest {
    @Nullable
    private final Long roomId;

    @NotNull
    private final Boolean showInNosecount;
}
