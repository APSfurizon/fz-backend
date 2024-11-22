package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddMembershipCardRequest {
    @Min(0L)
    private long userId;
}
