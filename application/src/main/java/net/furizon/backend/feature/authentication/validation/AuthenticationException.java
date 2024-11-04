package net.furizon.backend.feature.authentication.validation;

import net.furizon.backend.feature.authentication.AuthenticationErrorCode;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;

import static net.furizon.backend.feature.authentication.Const.EMAIL_PATTERN;

public class AuthenticationException {
    public static void validateEmailOrThrow(@NotNull String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(
                "Invalid email",
                AuthenticationErrorCode.EMAIL_INVALID.name()
            );
        }
    }
}
