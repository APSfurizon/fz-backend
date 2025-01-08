package net.furizon.backend.feature.pretix.objects.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixPaymentRefundRequest {
    @NotNull
    private final String amount;
    @JsonProperty("mark_canceled")
    private final boolean markCanceled = false;

    public PretixPaymentRefundRequest(@NotNull String amount) {
        this.amount = amount;
    }

    public PretixPaymentRefundRequest(long amount) {
        this.amount = PretixGenericUtils.fromPriceToString(amount, '.');
    }
}
