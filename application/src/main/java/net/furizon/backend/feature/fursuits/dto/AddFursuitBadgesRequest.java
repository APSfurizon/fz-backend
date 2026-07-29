package net.furizon.backend.feature.fursuits.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AddFursuitBadgesRequest {
    @NotNull
    private final Long targetUserId;
    @NotNull @Positive
    private final Integer quantity;
    @NotNull
    private final Boolean alreadyPaid;
}
