package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.responses.AuthenticationCodeResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetResetPwStatusUseCase implements UseCase<GetResetPwStatusUseCase.Input, AuthenticationCodeResponse> {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull AuthenticationCodeResponse executor(@NotNull Input input) {
        return new AuthenticationCodeResponse(
            input.user != null ? AuthenticationCodes.ALREADY_LOGGED_IN : (
                sessionAuthenticationManager.isResetPwRequestPending(input.pwResetId)
                    ? AuthenticationCodes.PW_RESET_STILL_PENDING
                    : AuthenticationCodes.PW_RESET_NOT_FOUND
            )
        );
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull UUID pwResetId
    ) {}
}
