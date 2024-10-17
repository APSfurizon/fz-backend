package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.action.RemoveSessionAction;
import net.furizon.backend.feature.authentication.dto.LogoutRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DestoySessionUseCase implements UseCase<LogoutRequest, Boolean> {
    private final RemoveSessionAction removeSessionAction;

    @Transactional
    @Override
    public @NotNull Boolean executor(@NotNull LogoutRequest input) {
        return null;
    }
}
