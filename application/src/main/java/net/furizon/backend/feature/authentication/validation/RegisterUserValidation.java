package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationErrorCode;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.finder.AuthenticationFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RegisterUserValidation {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private final AuthenticationFinder authenticationFinder;

    public void validate(@NotNull RegisterUserRequest input) {
        if (!EMAIL_PATTERN.matcher(input.getEmail()).matches()) {
            throw new ApiException(
                "Invalid email",
                AuthenticationErrorCode.EMAIL_INVALID.name()
            );
        }

        final var authentication = authenticationFinder.findByEmail(input.getEmail());
        if (authentication != null) {
            throw new ApiException(
                "User already exists with email: %s".formatted(input.getEmail()),
                AuthenticationErrorCode.EMAIL_ALREADY_REGISTERED.name()
            );
        }
    }
}
