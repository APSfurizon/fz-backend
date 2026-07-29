package net.furizon.backend.feature.pretix.objects.order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class AddPositionWithPaymentRequest {
    @NotNull private final String orderCode;
    private final long item;
    @JsonProperty("create_payment") private final boolean createPayment;

    @Nullable private final Integer quantity;
    @Nullable @JsonProperty("addon_to") private final Long addonTo;
    @Nullable private final Long variation;
    @Nullable private final String price;

    @Nullable @JsonProperty("check_quotas") private final Boolean checkQuotas;
    @Nullable @JsonProperty("notify_user") private final Boolean notifyUser;
}
