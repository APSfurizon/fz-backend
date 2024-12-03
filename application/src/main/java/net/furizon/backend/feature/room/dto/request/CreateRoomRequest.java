package net.furizon.backend.feature.room.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoomRequest {
    @NotNull
    @NotEmpty
    @Size(min = 1, max = 254)
    private final String name;
}
