package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ChangeNameToRoomRequest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RenameRoomUseCase implements UseCase<RenameRoomUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        log.info("User {} is changing the name of room {} to {}",
                requesterUserId, input.req.getRoomId(), input.req.getName());

        checks.assertInTimeframeToEditRooms();
        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId()
        );

        return roomLogic.changeRoomName(input.req.getName(), roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ChangeNameToRoomRequest req,
            @NotNull Event event
    ) {}
}
