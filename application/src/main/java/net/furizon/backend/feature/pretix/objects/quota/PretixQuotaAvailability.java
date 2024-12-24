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
