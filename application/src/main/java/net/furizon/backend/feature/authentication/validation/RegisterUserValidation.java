package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserValidation {
    public void validate(@NotNull RegisterUserRequest input) {

    }
}
