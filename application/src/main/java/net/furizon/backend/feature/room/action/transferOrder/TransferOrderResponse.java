package net.furizon.backend.feature.room.action.transferOrder;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
class TransferOrderResponse {
    @NotNull
    private final String newOrderCode;
}
