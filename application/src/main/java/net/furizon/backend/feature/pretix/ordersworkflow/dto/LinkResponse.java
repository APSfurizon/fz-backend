package net.furizon.backend.feature.pretix.ordersworkflow.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LinkResponse {
    @NotNull
    private final String link;
}
