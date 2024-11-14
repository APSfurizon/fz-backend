package net.furizon.backend.infrastructure.web.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ApiError {
    @NotNull
    private final String message;

    @NotNull
    private final String code;
}
