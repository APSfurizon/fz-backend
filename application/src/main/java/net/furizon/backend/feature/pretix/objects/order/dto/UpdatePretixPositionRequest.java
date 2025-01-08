package net.furizon.backend.feature.pretix.objects.order.dto;

import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;

@Data
public class UpdatePretixPositionRequest {
    private final String order;

    private final long item;

    private final String price;

    public UpdatePretixPositionRequest(@NotNull String order, long item, long price) {
        this.order = order;
        this.item = item;
        this.price = PretixGenericUtils.fromPriceToString(price, '.');
    }
}
