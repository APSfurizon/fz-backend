package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.responses.AuthenticationCodeResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetResetPwStatusUseCase implements UseCase<GetResetPwStatusUseCase.Input, AuthenticationCodeResponse> {

    @Override
    public @NotNull AuthenticationCodeResponse executor(@NotNull Input input) {
        return null;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull UUID pwResetId
    ) {}
}
