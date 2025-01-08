package net.furizon.backend.feature.authentication.dto.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ChangePasswordRequest {
    @Nullable
    private UUID resetPwId;

    @NotNull
    @NotEmpty
    @Size(min = 6)
    private final String newPassword;
}
