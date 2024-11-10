package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationErrorCode;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.finder.AuthenticationFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserValidation {
    private final AuthenticationFinder authenticationFinder;

    public void validate(@NotNull RegisterUserRequest input) {
        final var authentication = authenticationFinder.findByEmail(input.getEmail());
        if (authentication != null) {
            throw new ApiException(
                "User already exists with email: %s".formatted(input.getEmail()),
                AuthenticationErrorCode.EMAIL_ALREADY_REGISTERED.name()
            );
        }
    }
}
