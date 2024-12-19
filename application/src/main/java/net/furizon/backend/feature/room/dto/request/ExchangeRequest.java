package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class ExchangeRequest {
    @Null
    private Long sourceUserId;

    private long destUserId;
}
