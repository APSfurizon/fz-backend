package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.feature.user.objects.dto.UserDisplayDataResponse;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetUserDisplayDataUseCase implements UseCase<GetUserDisplayDataUseCase.Input,
        Optional<UserDisplayDataResponse>> {
    private final UserFinder userFinder;

    @Override
    public @NotNull Optional<UserDisplayDataResponse> executor(@NotNull GetUserDisplayDataUseCase.Input input) {
        User userFound = userFinder.findById(input.userId);
        return Optional.ofNullable(userFound != null ? new UserDisplayDataResponse(userFound) : null);
    }

    public record Input(long userId) {}
}
