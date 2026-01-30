package net.furizon.backend.feature.authentication.dto.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static net.furizon.backend.feature.authentication.Const.EMAIL_REGEX;

@Data
public class ChangeMailRequest {
    @Nullable
    private Long targetUserId;

    @NotNull
    @NotEmpty
    @Email(regexp = EMAIL_REGEX)
    private final String email;
}
