package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.action.updateShowInNosecount.UpdateShowInNosecountAction;
import net.furizon.backend.feature.user.dto.UpdateShowInNosecountRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateShowInNosecountUseCase implements UseCase<UpdateShowInNosecountRequest, Boolean> {
    @NotNull private final UpdateShowInNosecountAction updateShowInNosecount;

    @Override
    public @NotNull Boolean executor(@NotNull UpdateShowInNosecountRequest input) {
        log.info("Updating showInNosecount to user {} = {}", input.getUserId(), input.getShowInNosecount());
        return updateShowInNosecount.invoke(input.getUserId(), input.getShowInNosecount());
    }
}
