package net.furizon.backend.feature.pretix.order;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixOrder {
    @NotNull
    private final String code;

    @NotNull
    private final String secret;

    @NotNull
    private final String status;

    @NotNull
    private final List<PretixPosition> positions;
}
