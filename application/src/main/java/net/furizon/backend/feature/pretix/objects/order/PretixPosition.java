package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixPosition {
    @JsonProperty("item")
    private final int itemId;

    @JsonProperty("id")
    private final int positionId;

    @JsonProperty("variation")
    private final int variationId;

    @NotNull
    private final List<PretixAnswer> answers;
}
