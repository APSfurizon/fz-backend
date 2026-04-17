package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class RedeemCheckinResponse {
    @NotNull
    private final Status status;

    @Nullable
    private final CheckinErrorCodes reason;

    @Nullable
    @JsonProperty("reason_explanation")
    private final String message;

    @NotNull
    private final PretixPosition position;

    @NotNull
    @JsonProperty("require_attention")
    private final Boolean requireAttention;

    @NotNull
    @JsonProperty("checkin_texts")
    private final List<String> checkinTexts;

    //TODO add support for show_during_checkin questions

    public enum Status {
        @JsonProperty("ok")
        OK,
        @JsonProperty("incomplete")
        INCOMPLETE,
        @JsonProperty("error")
        ERROR
    }


}
