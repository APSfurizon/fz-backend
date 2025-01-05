package net.furizon.backend.feature.authentication.usecase;

import net.furizon.backend.feature.authentication.dto.requests.ChangePasswordRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChangePasswordUseCase implements UseCase<ChangePasswordUseCase.Input, Boolean> {

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        return true;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull ChangePasswordRequest req
    ) {}
}
