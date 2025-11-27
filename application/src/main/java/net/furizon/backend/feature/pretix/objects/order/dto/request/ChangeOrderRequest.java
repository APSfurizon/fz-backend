package net.furizon.backend.feature.pretix.objects.order.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeOrderRequest {

    // +++ PATCH POSITION +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("patch_positions")
    private List<PatchPositionImpl> patchPositions = null;

    public synchronized ChangeOrderRequest patchPosition(long positionId, @NotNull PatchPosition patchPosition) {
        if (patchPositions == null) {
            patchPositions = new ArrayList<>();
        }
        patchPositions.add(new PatchPositionImpl(positionId, patchPosition));
        return this;
    }

    @Data
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatchPosition {
        @Nullable private final Long item;
        @Nullable private final Long variation;
        @Nullable private final Long subevent;
        @Nullable private final String seat;
        @Nullable @JsonProperty("valid_from") private final OffsetDateTime validFrom;
        @Nullable @JsonProperty("valid_until") private final OffsetDateTime validUntil;
        @Nullable private String price;

        public PatchPosition(@Nullable Long item,
                             @Nullable Long variation,
                             @Nullable Long subevent,
                             @Nullable String seat,
                             long price,
                             @Nullable OffsetDateTime validFrom,
                             @Nullable OffsetDateTime validUntil) {
            this.item = item;
            this.variation = variation;
            this.subevent = subevent;
            this.seat = seat;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
            setPrice(price);
        }

        public PatchPosition setPrice(@Nullable final String price) {
            this.price = price;
            return this;
        }
        public PatchPosition setPrice(long price) {
            this.price = PretixGenericUtils.fromPriceToString(price, '.');
            return this;
        }
    }

    @Data
    @AllArgsConstructor
    private class PatchPositionImpl {
        private final long position;
        @NotNull private final PatchPosition body;
    }

    // +++ CANCEL POSITION +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("cancel_positions")
    private List<PositionIdImpl> cancelPositions = null;

    public synchronized ChangeOrderRequest cancelPosition(long positionId) {
        if (cancelPositions == null) {
            cancelPositions = new ArrayList<>();
        }
        cancelPositions.add(new PositionIdImpl(positionId));
        return this;
    }

    // +++ SPLIT POSITION +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("split_positions")
    private List<PositionIdImpl> splitPositions = null;

    public synchronized ChangeOrderRequest splitPosition(long positionId) {
        if (splitPositions == null) {
            splitPositions = new ArrayList<>();
        }
        splitPositions.add(new PositionIdImpl(positionId));
        return this;
    }

    // +++ CREATE POSITION +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("create_positions")
    private List<PushPretixPositionRequest> createPositions = null;

    public synchronized ChangeOrderRequest createPosition(PushPretixPositionRequest position) {
        if (createPositions == null) {
            createPositions = new ArrayList<>();
        }
        createPositions.add(position);
        return this;
    }

    // +++ PATCH FEES +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("patch_fees")
    private List<PatchFeeFather> patchFee = null;

    public synchronized ChangeOrderRequest patchFee(long feeId, long value) {
        if (patchFee == null) {
            patchFee = new ArrayList<>();
        }
        patchFee.add(new PatchFeeFather(feeId, new PatchFatherChild(value)));
        return this;
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatchFatherChild {
        @Nullable private final String value;

        public PatchFatherChild(long value) {
            this.value = PretixGenericUtils.fromPriceToString(value, '.');
        }
    }

    @Data
    @AllArgsConstructor
    private class PatchFeeFather {
        private final long fee;
        @NotNull private final PatchFatherChild body;
    }

    // +++ CANCEL FEE +++

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("cancel_fees")
    private List<FeeIdImpl> cancelFees = null;

    public synchronized ChangeOrderRequest cancelFee(long feeId) {
        if (cancelFees == null) {
            cancelFees = new ArrayList<>();
        }
        cancelFees.add(new FeeIdImpl(feeId));
        return this;
    }

    @Data
    @AllArgsConstructor
    private class FeeIdImpl {
        private final long fee;
    }

    // +++ CREATE FEE +++

    @Data
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateFeeRequest {
        @NotNull @JsonProperty("fee_type") private final FeeType feeType;
        @NotNull private final String description;
        @NotNull @JsonProperty("internal_type") private final String internalType = "";
        @JsonProperty("tax_rule") private final long taxRuleId;
        @NotNull private String value;

        public CreateFeeRequest(@NotNull FeeType feeType,
                                long value,
                                @NotNull String description,
                                long taxRuleId) {
            this.feeType = feeType;
            this.value = PretixGenericUtils.fromPriceToString(value, '.');
            this.description = description;
            this.taxRuleId = taxRuleId;
        }

        public CreateFeeRequest setValue(@NotNull final String value) {
            this.value = value;
            return this;
        }
        public CreateFeeRequest setValue(long value) {
            this.value = PretixGenericUtils.fromPriceToString(value, '.');
            return this;
        }
    }

    public enum FeeType {
        PAYMENT,
        SHIPPING,
        SERVICE,
        CANCELLATION,
        INSURANCE,
        LATE,
        OTHER,
        GIFTCARD
    }

    // +++ RECALCULATE TAXES +++

    public enum RecalculateTaxes {
        KEEP_NET,
        KEEP_GROSS,
        NONE
    }

    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("recalculate_taxes")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private RecalculateTaxes recalculateTaxes = null;

    public ChangeOrderRequest recalculateTaxes(@Nullable RecalculateTaxes recalculateTaxes) {
        this.recalculateTaxes = recalculateTaxes;
        if (recalculateTaxes != null && recalculateTaxes == RecalculateTaxes.NONE) {
            this.recalculateTaxes = null;
        }
        return this;
    }

    // +++ SEND EMAIL +++

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("send_email")
    private boolean sendEmail = false;

    public ChangeOrderRequest sendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
        return this;
    }

    // +++ REISSUE INVOICE +++

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("reissue_invoice")
    private boolean reissueInvoice = false;

    public ChangeOrderRequest reissueInvoice(boolean reissueInvoice) {
        this.reissueInvoice = reissueInvoice;
        return this;
    }

    // +++ GENERAL +++

    @Data
    @AllArgsConstructor
    private class PositionIdImpl {
        private final long position;
    }
}
