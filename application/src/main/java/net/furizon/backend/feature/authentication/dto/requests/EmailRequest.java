package net.furizon.backend.feature.authentication.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailRequest {
    @NotNull @NotEmpty
    private final String email;
}
