package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.furizon.backend.infrastructure.GeneralConsts;

@Data
public class ChangeNameToRoomRequest {
    @Nullable
    private final Long roomId;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 254)
    @Pattern(regexp = GeneralConsts.NAME_REGEX)
    private final String name;
}
