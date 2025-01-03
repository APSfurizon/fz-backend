package net.furizon.backend.feature.room.dto.request;

import lombok.Data;

@Data
public class UpdateExchangeStatusRequest {
    private final long exchangeId;
    private final boolean confirm;
}
