package net.furizon.backend.feature.pretix.order.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixFileUploadResponse {
    @NotNull
    private final String id;
}
