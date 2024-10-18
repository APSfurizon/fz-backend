package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LoginRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAuthenticationTokenUseCase implements UseCase<LoginRequest, String> {
    @Override
    public @NotNull String executor(@NotNull LoginRequest input) {
        return "";
    }
}
