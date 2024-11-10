package net.furizon.backend.feature.authentication.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static net.furizon.backend.feature.authentication.Const.EMAIL_REGEX;

@Data
public class RegisterUserRequest {
    @NotNull
    @NotEmpty
    @Email(regexp = EMAIL_REGEX)
    private final String email;

    @NotNull
    @NotEmpty
    @Size(min = 6)
    private final String password;

    @NotNull
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\- ]{3,20}$")
    private final String fursonaName;

    @NotNull
    @Valid
    private final PersonalUserInformation personalUserInformation;
}
