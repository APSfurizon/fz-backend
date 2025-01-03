package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import net.furizon.backend.feature.room.dto.ExchangeAction;

@Data
public class ExchangeRequest {
    @Null
    private final Long sourceUserId;

    private final long destUserId;
    @NotNull
    private final ExchangeAction action;
}
