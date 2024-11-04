package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.action.createSession.CreateSessionAction;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.validation.CreateLoginSessionValidation;
import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import net.furizon.backend.infrastructure.security.token.encoder.TokenEncoder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionUseCase implements UseCase<CreateLoginSessionUseCase.Input, LoginResponse> {
    private final CreateLoginSessionValidation validation;

    private final CreateSessionAction createSessionAction;

    private final TokenEncoder tokenEncoder;

    @Transactional
    @Override
    public @NotNull LoginResponse executor(@NotNull CreateLoginSessionUseCase.Input input) {
        final var userId = validation.validateAndGetUserId(input);
        final var sessionId = createSessionAction.invoke(
            userId,
            input.clientIp,
            input.userAgent
        );

        return new LoginResponse(
            userId,
            tokenEncoder.encode(
                TokenMetadata.builder()
                    .sessionId(sessionId)
                    .build()
            )
        );
    }

    public record Input(
        @NotNull String email,
        @NotNull String password,
        @NotNull String clientIp,
        @Nullable String userAgent
    ) {}
}
