package net.furizon.backend.feature.pretix.ordersworkflow.dto.response;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class OrderDataResponse {
    @NotNull
    private String code;

    @Nullable
    private String checkinSecret;

    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private Sponsorship sponsorship;

    @Nullable
    private ExtraDays extraDays;

    @Nullable
    private Board board;

    private boolean isDailyTicket;
    @Nullable
    private final Set<LocalDate> dailyDays;

    private short totalFursuits;

    @Nullable
    private RoomData room;
}
