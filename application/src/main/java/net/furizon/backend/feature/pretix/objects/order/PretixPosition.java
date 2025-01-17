package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PretixPosition {
    @JsonProperty("item")
    private long itemId;

    @JsonProperty("id")
    private final long positionId;

    @JsonProperty("positionid")
    private final long positionPosid;

    @Nullable
    @JsonProperty("variation")
    private final Long variationId;

    @NotNull
    private final List<PretixAnswer> answers;

    @NotNull //Price ALWAYS include taxes
    private String price;
    @NotNull
    @JsonProperty("tax_rate")
    private final String taxRate;
    @NotNull
    @JsonProperty("tax_value")
    private final String taxValue;

    private final boolean canceled;

    @Nullable
    private final Long subevent;

    @Nullable
    private final String seat;

    @Nullable
    @JsonProperty("attendee_email")
    private final String email;

    @Nullable
    @JsonProperty("attendee_name")
    private final String name;
    @Nullable
    @JsonProperty("attendee_name_parts")
    private final Map<String, String> nameParts;

    @Nullable
    private final String company;

    @Nullable
    private final String street;

    @Nullable
    private final String zipcode;

    @Nullable
    private final String city;

    @Nullable
    private final String country;

    @Nullable
    private final String state;

    @Nullable
    @JsonProperty("valid_from")
    private final OffsetDateTime validFrom;

    @Nullable
    @JsonProperty("valid_until")
    private final OffsetDateTime validUntil;
}
