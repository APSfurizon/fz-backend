package net.furizon.backend.feature.room.dto.response;

import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class RoomAvailabilityInfoResponse implements Comparable<RoomAvailabilityInfoResponse> {
    @NotNull
    private final RoomData data;
    @NotNull
    private final String price;
    @Nullable
    private final Long remaining;

    @Override
    public int compareTo(@NotNull RoomAvailabilityInfoResponse o) {
        int v = data.compareTo(o.data);
        if (v == 0) {
            long thisPrice = PretixGenericUtils.fromStrPriceToLong(price);
            long otherPrice = PretixGenericUtils.fromStrPriceToLong(o.price);
            v = (int) (thisPrice - otherPrice);
        }
        return v;
    }
}
