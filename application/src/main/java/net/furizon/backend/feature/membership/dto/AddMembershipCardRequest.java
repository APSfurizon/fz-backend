package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMembershipCardRequest {
    @NotNull
    @Min(0L)
    private final Long userId;
}
