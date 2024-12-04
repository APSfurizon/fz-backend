package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.dto.UserDisplayDataResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetUserDisplayDataUseCase implements UseCase<Long, Optional<UserDisplayDataResponse>> {
    private final UserFinder userFinder;

    @Override
    public @NotNull Optional<UserDisplayDataResponse> executor(@NotNull Long userId) {
        return Optional.ofNullable(userFinder.getDisplayUser(userId));
    }
}
