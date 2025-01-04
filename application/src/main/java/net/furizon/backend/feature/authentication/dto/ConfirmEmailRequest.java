package net.furizon.backend.feature.authentication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ConfirmEmailRequest {
    @NotNull
    private final UUID confirmationId;
}
