package net.furizon.backend.feature.room.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class ExchangeConfirmationStatusResponse {
    @NotNull private final UserDisplayData sourceUser;
    private final boolean sourceConfirmed;
    @NotNull private final UserDisplayData targetUser;
    private final boolean targetConfirmed;

    @NotNull private final ExchangeAction action;

    @Nullable private final OrderDataResponse fullOrderExchange;
    @Nullable private final RoomData sourceRoomExchange;
    @Nullable private final ExtraDays sourceExtraDays;
    @Nullable private final RoomData targetRoomExchange;
    @Nullable private final ExtraDays targetExtraDays;
}
