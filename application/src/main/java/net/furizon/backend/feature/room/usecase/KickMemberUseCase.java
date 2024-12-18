package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KickMemberUseCase implements UseCase<KickMemberUseCase.Input, Boolean> {
    @NotNull
    private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull KickMemberUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;
        
        RoomGuest guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();

        roomId = checks.getRoomIdAndAssertPermissionsOnRoom(requesterUserId, event, roomId);
        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(guest.getUserId(), roomId);

        return roomLogic.kickFromRoom(guestId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req,
            @NotNull Event event
    ) {}
}
