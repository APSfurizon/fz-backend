package net.furizon.backend.feature.authentication.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static net.furizon.backend.feature.authentication.Const.EMAIL_REGEX;

@Data
public class LoginRequest {
    @NotNull
    @NotEmpty
    @Email(regexp = EMAIL_REGEX)
    private final String email;

    // NOTICE: All passwords needs to be named "password" to be
    // mathced by net.furizon.backend.infrastructure.configuration.LogbookFilterConfiguration
    // and be hidden in logs
    @NotNull
    @NotEmpty
    @Size(min = 6)
    private final String password;
}
