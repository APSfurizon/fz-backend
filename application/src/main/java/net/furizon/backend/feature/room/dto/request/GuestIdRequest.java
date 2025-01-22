package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GuestIdRequest {
    @NotNull
    @Min(0L)
    private final Long guestId;
}
