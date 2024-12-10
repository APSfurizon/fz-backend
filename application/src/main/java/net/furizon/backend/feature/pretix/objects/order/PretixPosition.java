package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixPosition {
    @JsonProperty("item")
    private final long itemId;

    @JsonProperty("id")
    private final long positionId;

    @JsonProperty("variation")
    private final long variationId;

    @NotNull
    private final List<PretixAnswer> answers;

    private final boolean canceled;
}
