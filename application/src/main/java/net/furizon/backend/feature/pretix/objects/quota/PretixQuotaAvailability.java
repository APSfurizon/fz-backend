package net.furizon.backend.feature.pretix.objects.quota;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class PretixQuotaAvailability {
    private boolean available;
    @Getter(AccessLevel.NONE)
    @JsonProperty("available_number")
    private Long remaining;
    @Getter(AccessLevel.NONE)
    @JsonProperty("total_size")
    private Long total;

    @JsonProperty("pending_orders")
    private final long pendingOrders;
    @JsonProperty("paid_orders")
    private final long paidOrders;
    @JsonProperty("exited_orders")
    private final long exitedOrders;
    @JsonProperty("cart_positions")
    private final long cartPositions;
    @JsonProperty("blocking_vouchers")
    private final long blockingVouchers;
    @JsonProperty("waiting_list")
    private final long waitingList;

    /*
    This method may differ from remaining, since it can show extra quota used
     */
    public long calcEffectiveRemainig() {
        return getTotal() - getUsed();
    }

    public long getUsed() {
        //IGNORE WAITING LIST, otherwise pendingOrders are counted twice for some reasons
        return pendingOrders + paidOrders + exitedOrders + cartPositions + blockingVouchers /*+ waitingList*/;
    }

    public long getRemaining() {
        return remaining == null ? Long.MAX_VALUE : remaining;
    }

    public long getTotal() {
        return total == null ? Long.MAX_VALUE : total;
    }

    public boolean isUnlimited() {
        return remaining == null || total == null;
    }
}
