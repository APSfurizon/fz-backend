package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Data
public class UserEmailData {
    private final long userId;
    @NotNull
    private final String email;
    @NotNull
    private final String fursonaName;
    @NotNull
    private final Locale language;
}
