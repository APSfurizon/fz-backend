package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.SetShowInNosecountRequest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetShowInNosecountUseCase implements UseCase<SetShowInNosecountUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        log.info("User {} is setting showOnNosecount={} on room {}",
                requesterUserId, input.req.getShowInNosecount(), input.req.getRoomId());

        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId()
        );
        return roomLogic.setShowInNosecount(input.req.getShowInNosecount(), roomId);
    }

    public record Input(
            @NotNull SetShowInNosecountRequest req,
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
