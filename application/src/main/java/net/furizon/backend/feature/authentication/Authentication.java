package net.furizon.backend.feature.authentication;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Builder
@Data
@RequiredArgsConstructor
public class Authentication {
    private final long id;

    @NotNull
    private final String email;

    @Nullable
    private final OffsetDateTime mailVerificationCreationMs;

    private final boolean isDisabled;

    @NotNull
    private final String hashedPassword;

    @Nullable
    private final String authToken;

    public boolean is2FaEnabled() {
        return authToken != null;
    }

    private final long userId;
}
