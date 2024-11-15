package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetEditOrderLink implements UseCase<FurizonUser, LinkResponse> {
    @Override
    public @NotNull LinkResponse executor(@NotNull FurizonUser user) {
        return null;
    }

}
