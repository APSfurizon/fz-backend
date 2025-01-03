package net.furizon.backend.feature.room.dto.request;

import lombok.Data;

@Data
public class GetExchangeConfirmationStatusRequest {
    private final long exchangeId;
}
