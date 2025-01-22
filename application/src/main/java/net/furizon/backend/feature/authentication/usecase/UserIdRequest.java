package net.furizon.backend.feature.authentication.usecase;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserIdRequest {
    @NotNull
    private final Long userId;
}
