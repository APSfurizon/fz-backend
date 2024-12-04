package net.furizon.backend.feature.pretix.ordersworkflow.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Data
@Builder
public class OrderDataResponse {
    @NotNull
    private String code;

    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private Sponsorship sponsorship;

    @Nullable
    private ExtraDays extraDays;

    private boolean isDailyTicket;
    @Nullable
    private final Set<Integer> dailyDays;

    @Nullable
    private RoomDataResponse room;
}
