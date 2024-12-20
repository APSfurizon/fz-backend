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

    @NotNull //Price ALWAYS include taxes
    private final String price;
    @NotNull
    @JsonProperty("tax_rate")
    private final String taxRate;
    @NotNull
    @JsonProperty("tax_value")
    private final String taxValue;

    private final boolean canceled;
}
