package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class CreateRoomRequest {
    @NotNull
    @NotEmpty
    @Size(min = 1, max = 254)
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{4,63}$")
    private final String name;

    @Nullable
    private final Long userId;
}
