package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserMutationUseCase implements UseCase<String, User> {
    private final UserFinder userFinder;

    @Override
    public @NotNull User executor(@NotNull String input) {
        // ... logic behind of creation;
        return User.builder()
            .id(1)
            .lastname("")
            .firstname("")
            .build();
    }
}
