package net.furizon.backend.feature.pretix.objects.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixPayment {
    @JsonProperty("local_id")
    private final long id;
    @NotNull
    private final PaymentState state;
    @NotNull
    private final String amount;
    @NotNull
    private final String provider;

    public enum PaymentState {
        @JsonProperty("created")
        CREATED,
        @JsonProperty("pending")
        PENDING,
        @JsonProperty("confirmed")
        CONFIRMED,
        @JsonProperty("canceled")
        CANCELED,
        @JsonProperty("failed")
        FAILED,
        @JsonProperty("refunded")
        REFUNDED
    }
}
