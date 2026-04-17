package net.furizon.backend.feature.pretix.objects.checkins.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.action.invalidateCheckin.PretixInvalidateCheckinAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelCheckinUseCase implements UseCase<CancelCheckinUseCase.Input, Boolean> {
    @NotNull
    private final PretixInvalidateCheckinAction invalidateCheckinAction;

    @Override
    public @NotNull Boolean executor(@NotNull CancelCheckinUseCase.Input input) {
        log.info("User {} is cancelling checkin with nonce {}", input.user.getUserId(), input.nonce);
        return invalidateCheckinAction.invoke(
                input.event.getOrganizerAndEventPair().getOrganizer(),
                input.nonce,
                input.checkinListIds,
                input.explanation
        );
    }

    public record Input(
            @NotNull String nonce,
            @NotNull List<Long> checkinListIds,
            @Nullable String explanation,
            @NotNull Event event,
            @NotNull FurizonUser user
    ) {}
}
