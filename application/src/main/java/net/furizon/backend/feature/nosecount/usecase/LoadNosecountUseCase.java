package net.furizon.backend.feature.nosecount.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.FursuitCountResponse;
import net.furizon.backend.feature.nosecount.finder.CountsFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadNosecountUseCase implements UseCase<Long, FursuitCountResponse> {
    @NotNull private final CountsFinder countsFinder;

    @Override
    public @NotNull FursuitCountResponse executor(@NotNull Long input) {
        return null;
    }
}
