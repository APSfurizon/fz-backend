package net.furizon.backend.feature.authentication;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Builder
@Data
@RequiredArgsConstructor
public class Authentication {
    private final long id;

    @NotNull
    private final String email;

    private final boolean isVerified;

    private final boolean isTwoFactorEnabled;

    private final boolean isDisabled;

    private final boolean isFrom0Auth;

    @NotNull
    private final String hashedPassword;

    private final long userId;
}
