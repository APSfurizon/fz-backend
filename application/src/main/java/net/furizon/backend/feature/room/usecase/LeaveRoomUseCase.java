package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveRoomUseCase implements UseCase<LeaveRoomUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull LeaveRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        RoomGuest guest = checks.getRoomGuestObjFromUserEventAndAssertItExistsAndConfirmed(
                requesterUserId,
                event
        );
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();

        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(guest.getUserId(), roomId);
        checks.assertIsGuestObjOwnerOrAdmin(guest, requesterUserId);

        return roomLogic.leaveRoom(guest.getGuestId());
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
