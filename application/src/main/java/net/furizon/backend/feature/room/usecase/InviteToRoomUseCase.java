package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor

public class InviteToRoomUseCase implements UseCase<InviteToRoomUseCase.Input, RoomGuest> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;


    @Override
    public @NotNull RoomGuest executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        long targetUserId = input.req.getUserId();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        boolean isAdmin = true; //TODO [ADMIN_CHECK}

        checks.assertUserHasOrderAndItsNotDaily(targetUserId, event);
        checks.assertUserDoesNotOwnAroom(targetUserId, event);

        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId()
        );
        checks.assertUserIsNotRoomOwner(targetUserId, roomId);
        checks.assertUserIsNotInvitedToRoom(targetUserId, roomId);

        boolean forceExit = input.req.getForceExit() == null ? false : input.req.getForceExit() && isAdmin;
        if (!forceExit) {
            checks.assertUserIsNotInRoom(targetUserId, event);
        }

        checks.assertRoomNotConfirmed(roomId);
        checks.assertRoomNotFull(roomId, false);
        checks.assertOrderIsPaid(targetUserId, event);

        boolean force = input.req.getForce() == null ? false : input.req.getForce() && isAdmin;
        long guestId = roomLogic.invitePersonToRoom(targetUserId, roomId, event, force, forceExit);

        return new RoomGuest(guestId, targetUserId, roomId, false);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull InviteToRoomRequest req,
            @NotNull Event event
    ) {}
}
