package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class RedeemCheckinRequest {
    @NotNull
    private final String secret;

    @NotNull
    @JsonProperty("source_type")
    private final String sourceType = "fz-backend";

    @NotNull
    private final List<Long> lists;

    @NotNull
    private final Type type = Type.ENTRY;

    private final boolean force = false;

    @JsonProperty("questions_supported")
    private final boolean questionsSupported = false;

    @JsonProperty("ignore_unpaid")
    private final boolean ignoreUnpaid = false;

    @NotNull
    private final String nonce;

    private final boolean useOrderLocale = false;

    public enum Type {
        @JsonProperty("entry")
        ENTRY,
        @JsonProperty("exit")
        EXIT
    }
}
