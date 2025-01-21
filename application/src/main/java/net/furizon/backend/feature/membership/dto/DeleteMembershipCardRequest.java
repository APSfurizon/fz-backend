package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteMembershipCardRequest {
    @NotNull
    private final Long cardId;
}
