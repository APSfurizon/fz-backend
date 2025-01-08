package net.furizon.backend.feature.authentication.dto.responses;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.authentication.AuthenticationCodes;

@Data
public class AuthenticationCodeResponse {
    @NotNull
    private final AuthenticationCodes code;
}
