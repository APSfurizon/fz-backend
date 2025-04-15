package net.furizon.backend.feature.authentication.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DestroySessionRequest {
    @NotNull private final String sessionId;
}
