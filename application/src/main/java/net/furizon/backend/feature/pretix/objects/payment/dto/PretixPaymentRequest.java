package net.furizon.backend.feature.pretix.objects.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;

import java.time.OffsetDateTime;

@Data
public class PretixPaymentRequest {
    private final String state = "confirmed";
    private final String amount;
    @JsonProperty("payment_date")
    private final String paymentDate = OffsetDateTime.now().format(PretixGenericUtils.PRETIX_DATETIME_FORMAT);
    private final InfoObj info;
    @JsonProperty("send_email")
    private final boolean sendEmail = false;
    private final String provider = "fzbackend-manual";

    public PretixPaymentRequest(String amount, String comment) {
        this.amount = amount;
        this.info = new InfoObj(comment);
    }

    @Data
    public static class InfoObj {
        @JsonProperty("issued_by")
        private final String issuedBy = "fz-backend";
        private final String comment;
        public InfoObj(String comment) {
            this.comment = comment;
        }
    }
}
