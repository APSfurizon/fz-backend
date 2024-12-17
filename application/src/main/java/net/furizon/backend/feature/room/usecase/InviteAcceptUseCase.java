package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
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
public class InviteAcceptUseCase implements UseCase<InviteAcceptUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;

        checks.assertUserHasOrderAndItsNotDaily(requesterUserId, event);

        RoomGuestResponse guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        long roomId = guest.getRoomId();

        checks.assertRoomNotFull(roomId);
        checks.assertRoomNotConfirmed(roomId);
        checks.assertGuestIsNotConfirmed(guest);
        checks.assertUserIsNotInRoom(requesterUserId, event);
        checks.assertUserIsNotRoomOwner(requesterUserId, roomId);
        checks.assertUserDoesNotOwnAroom(requesterUserId, event);
        checks.assertIsGuestObjOwnerOrAdmin(guest, requesterUserId);

        return roomLogic.inviteAccept(guestId, roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req,
            @NotNull Event event
    ) {}
}
