package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.validation.CreateLoginSessionValidation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionUseCase implements UseCase<LoginRequest, LoginResponse> {
    private final CreateLoginSessionValidation validation;

    @Override
    public @NotNull LoginResponse executor(@NotNull LoginRequest input) {
        final var userId = validation.validateAndGetUserId(input);

        return new LoginResponse(0, "");
    }
}
