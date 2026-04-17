package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

@Getter
public enum CheckinErrorCodes {
    @JsonProperty("invalid")
    TICKET_INVALID("checkin.redeem.errors.invalid"),
    @JsonProperty("unpaid")
    TICKET_UNPAID("checkin.redeem.errors.unpaid"),
    @JsonProperty("blocked")
    TICKET_BLOCKED("checkin.redeem.errors.blocked"),
    @JsonProperty("invalid_time")
    TICKET_INVALID_TIME("checkin.redeem.errors.invalid_time"),
    @JsonProperty("canceled")
    TICKET_CANCELED("checkin.redeem.errors.canceled"),
    @JsonProperty("already_redeemed")
    ALREADY_REDEEMED("checkin.redeem.errors.already_redeemed"),
    @JsonProperty("product")
    PRODUCT_CANNOT_BE_SCANNED("checkin.redeem.errors.product"),
    @JsonProperty("rules")
    BLOCKED_BY_RULES("checkin.redeem.errors.rules"),
    @JsonProperty("ambiguous")
    MULTIPLE_MATCH("checkin.redeem.errors.ambiguous"),
    @JsonProperty("revoked")
    TICKET_REVOKED("checkin.redeem.errors.revoked"),
    @JsonProperty("unapproved")
    ORDER_NOT_APPROVED("checkin.redeem.errors.unapproved"),
    @JsonProperty("error")
    INTERNAL_ERROR("checkin.redeem.errors.error");

    @NotNull
    private final TranslatableValue errorMessage;

    CheckinErrorCodes(@PropertyKey(resourceBundle = "errors.errors") @NotNull String key) {
        errorMessage = TranslatableValue.ofError(key);
    }
}
