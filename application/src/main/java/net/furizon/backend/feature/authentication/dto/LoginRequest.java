package net.furizon.backend.feature.authentication.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LoginRequest {
    @NotNull
    private final String email;

    @NotNull
    private final String password;
}
