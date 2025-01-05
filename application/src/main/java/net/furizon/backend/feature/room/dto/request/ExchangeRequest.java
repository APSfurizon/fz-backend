package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.room.dto.ExchangeAction;

@Data
public class ExchangeRequest {
    @Nullable
    private final Long sourceUserId;

    private final long recipientUserId;

    @NotNull
    private final ExchangeAction action;
}
