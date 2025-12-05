package net.furizon.backend.feature.pretix.cache.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ReloadStructRequest {
    @NotNull
    private final Boolean reloadAlsoPastEvents;
}
