package net.furizon.backend.feature.room.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminSanityChecksResponse {
    @NotNull
    private final List<String> errors;
}
