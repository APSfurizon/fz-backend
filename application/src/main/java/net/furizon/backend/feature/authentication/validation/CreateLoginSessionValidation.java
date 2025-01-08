package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionValidation {
    private final SessionAuthenticationManager sessionAuthenticationManager;

    private final SecurityConfig securityConfig;

    private final PasswordEncoder passwordEncoder;

    public long validateAndGetUserId(@NotNull LoginUserUseCase.Input input) {
        final var authentication = sessionAuthenticationManager.findAuthenticationByEmail(input.email());
        if (authentication == null) {
            //Using the same exception to not leak registered emails
            throw createInvalidCredentialsException();
        }

        if (authentication.isDisabled()) {
            throw new ApiException(
                "Not possible to login",
                AuthenticationCodes.AUTHENTICATION_IS_DISABLED
            );
        }

        if (authentication.getMailVerificationCreationMs() != null) {
            throw new ApiException(
                    "Email confirmation is still pending",
                    AuthenticationCodes.CONFIRMATION_STILL_PENDING
            );
        }

        final var passwordMatches = passwordEncoder.matches(
            securityConfig.getPasswordSalt() + input.password(),
            authentication.getHashedPassword()
        );
        if (!passwordMatches) {
            throw createInvalidCredentialsException();
        }

        return authentication.getUserId();
    }

    private final ApiException createInvalidCredentialsException() {
        return new ApiException(
                "Invalid Credentials",
                AuthenticationCodes.INVALID_CREDENTIALS
        );
    }
}
