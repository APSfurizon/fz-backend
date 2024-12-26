package net.furizon.backend.feature.pretix.objects.refund;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixRefund {
    @JsonProperty("local_id")
    private final long id;
    @NotNull
    private final RefundState state;
    @NotNull
    private final RefundSource source;
    @NotNull
    private final String amount;
    @NotNull
    private final String provider;

    public enum RefundState {
        @JsonProperty("created")
        CREATED,
        @JsonProperty("transit")
        TRANSIT,
        @JsonProperty("external")
        EXTERNAL,
        @JsonProperty("canceled")
        CANCELED,
        @JsonProperty("failed")
        FAILED,
        @JsonProperty("done")
        DONE
    }

    public enum RefundSource {
        @JsonProperty("buyer")
        BUYER,
        @JsonProperty("admin")
        ADMIN,
        @JsonProperty("external")
        EXTERNAL
    }
}
