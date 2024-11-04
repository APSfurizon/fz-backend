package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationErrorCode;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.finder.AuthenticationFinder;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionValidation {
    private final AuthenticationFinder authenticationFinder;

    private final SecurityConfig securityConfig;

    private final PasswordEncoder passwordEncoder;

    public long validateAndGetUserId(@NotNull LoginRequest input) {
        AuthenticationException.validateEmailOrThrow(input.getEmail());

        final var authentication = authenticationFinder.findByEmail(input.getEmail());
        if (authentication == null) {
            throw new ApiException(
                "User not found",
                AuthenticationErrorCode.EMAIL_NOT_REGISTERED.name()
            );
        }

        final var passwordMatches = passwordEncoder.matches(
            securityConfig.getPasswordSalt() + input.getPassword(),
            authentication.getHashedPassword()
        );
        if (!passwordMatches) {
            throw new ApiException(
                "Invalid Credentials",
                AuthenticationErrorCode.INVALID_CREDENTIALS.name()
            );
        }

        return authentication.getUserId();
    }
}
