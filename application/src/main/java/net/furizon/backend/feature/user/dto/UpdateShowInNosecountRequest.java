package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class UpdateShowInNosecountRequest {
    @NotNull private final Long userId;
    @NotNull private final Boolean showInNosecount;
}
