package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
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
        long guestId = input.req.getGuestId();

        //checks.assertUserHasOrderAndItsNotDaily(requesterUserId, event);

        RoomGuestResponse guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();

        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(guest.getUserId(), roomId);
        checks.assertIsGuestObjOwnerOrAdmin(guest, requesterUserId);

        return roomLogic.leaveRoom(guestId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req
    ) {}
}
