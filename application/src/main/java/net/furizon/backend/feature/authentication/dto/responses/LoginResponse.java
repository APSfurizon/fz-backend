package net.furizon.backend.feature.authentication.dto.responses;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LoginResponse {
    private final long userId;

    @NotNull
    private final String accessToken;
}
