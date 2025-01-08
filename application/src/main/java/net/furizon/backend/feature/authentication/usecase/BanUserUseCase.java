package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BanUserUseCase implements UseCase<BanUserUseCase.Input, Boolean> {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        log.info("Admin {} is banning user  {}", input.user.getUserId(), input.req.getUserId());
        sessionAuthenticationManager.disableUser(input.req.getUserId());
        return true;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull UserIdRequest req
    ) {}
}
