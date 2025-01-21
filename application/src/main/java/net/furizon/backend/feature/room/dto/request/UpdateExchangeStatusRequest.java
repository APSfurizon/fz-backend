package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateExchangeStatusRequest {
    @NotNull private final Long exchangeId;
    @NotNull private final Boolean confirm;
}
