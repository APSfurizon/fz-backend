package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetMembershipCardRegistrationStatusRequest {
    @NotNull
    @Min(0L)
    private final Long membershipCardId;

    @NotNull
    private final Boolean registered;
}
