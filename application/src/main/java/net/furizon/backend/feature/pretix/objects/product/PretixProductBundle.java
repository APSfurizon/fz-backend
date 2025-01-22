package net.furizon.backend.feature.pretix.objects.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.Nullable;

@Data
public class PretixProductBundle {
    @JsonProperty("bundled_item")
    private final long id;
    @JsonProperty("bundled_variation")
    @Nullable private final Long variationId;
    @JsonProperty("count")
    private final long quantity; //Final price = count * price
    @JsonProperty("designated_price")
    private final long singleItemPrice;

    public long getTotalPrice() {
        return quantity * singleItemPrice;
    }

    public PretixProductBundle(
            @JsonProperty("bundled_item") long id,
            @JsonProperty("bundled_variation") @Nullable Long variationId,
            @JsonProperty("count") long count,
            @JsonProperty("designated_price") String singleItemPrice
    ) {
        this.id = id;
        this.variationId = variationId;
        this.quantity = count;
        this.singleItemPrice = PretixGenericUtils.fromStrPriceToLong(singleItemPrice);
    }
}
