package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class PretixOrder {
    @NotNull
    private final String code;

    @NotNull
    private final String secret;

    @NotNull
    private final String status;

    @Nullable
    private final String email;
    @Nullable
    private final String phone;
    @Nullable
    private final String customer;
    @Nullable
    private final String locale;

    @NotNull
    private final List<PretixPayment> payments;

    @NotNull
    private final List<PretixPosition> positions;

    @Nullable
    private final String comment;

    @JsonProperty("checkin_attention")
    private final boolean checkinRequiresAttention;
    @Nullable
    @JsonProperty("checkin_text")
    private final String checkinText;
}
