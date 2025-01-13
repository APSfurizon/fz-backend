package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixPosition {
    @JsonProperty("item")
    private long itemId;

    @JsonProperty("id")
    private final long positionId;

    @JsonProperty("positionid")
    private final long positionPosid;

    @JsonProperty("variation")
    private final long variationId;

    @NotNull
    private final List<PretixAnswer> answers;

    @NotNull //Price ALWAYS include taxes
    private String price;
    @NotNull
    @JsonProperty("tax_rate")
    private final String taxRate;
    @NotNull
    @JsonProperty("tax_value")
    private final String taxValue;

    private final boolean canceled;
}
