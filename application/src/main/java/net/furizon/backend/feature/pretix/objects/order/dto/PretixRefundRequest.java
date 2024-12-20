package net.furizon.backend.feature.pretix.objects.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PretixRefundRequest {
    private final String state = "done";
    private final String source = "external";
    private final String amount;
    private final String comment;
    private final String provider = "manual";
    @JsonProperty("mark_canceled")
    private final boolean markCanceled = false;
    @JsonProperty("mark_pending")
    private final boolean markPending = false;

    public PretixRefundRequest(String amount, String comment) {
        this.comment = comment;
        this.amount = amount;
    }
}
