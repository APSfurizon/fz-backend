package net.furizon.backend.feature.pretix.objects.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushPretixPositionRequest {

    // MANDATORY TO CREATE A NEW POSITION
    @NotNull
    @JsonProperty("order")
    private final String orderCode;

    @Nullable
    @JsonProperty("addon_to")
    private final Long addonTo;

    private long item;

    //OPTIONAL DATA

    @Nullable
    private final Long variation;

    @Nullable
    private final Long subevent;

    @Nullable
    private final String seat;

    @Nullable
    private String price;
    public PushPretixPositionRequest setPrice(@Nullable final String price) {
        this.price = price;
        return this;
    }
    public PushPretixPositionRequest setPrice(long price) {
        this.price = PretixGenericUtils.fromPriceToString(price, '.');
        return this;
    }

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
    private final List<PretixAnswer> answers;

    @Nullable
    @JsonProperty("valid_from")
    private final OffsetDateTime validFrom;

    @Nullable
    @JsonProperty("valid_until")
    private final OffsetDateTime validUntil;

    public PushPretixPositionRequest(@NotNull final String orderCode, @Nullable final Long addonTo, final long item) {
        this.orderCode = orderCode;
        this.addonTo = addonTo;
        this.item = item;

        variation = null;
        subevent = null;
        seat = null;
        price = null;
        email = null;
        name = null;
        nameParts = null;
        company = null;
        street = null;
        zipcode = null;
        city = null;
        country = null;
        state = null;
        answers = null;
        validFrom = null;
        validUntil = null;
    }
}
