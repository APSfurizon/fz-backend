package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class UserEmailData {
    private final long userId;
    @NotNull
    private final String email;
    @NotNull
    private final String fursonaName;
}
