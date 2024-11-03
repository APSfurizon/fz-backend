package net.furizon.backend.feature.authentication.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class RegisterUserRequest {
    @NotNull
    private final String email;

    @NotNull
    private final String password;

    @Nullable
    private final String fursuitName;
}
