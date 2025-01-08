package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.InviteToRoomResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor

public class InviteToRoomUseCase implements UseCase<InviteToRoomUseCase.Input, InviteToRoomResponse> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;


    @Override
    @Transactional
    public @NotNull InviteToRoomResponse executor(@NotNull Input input) {
        Set<RoomGuest> toReturn = new HashSet<>();
        long requesterUserId = input.user.getUserId();
        Set<Long> targetUserIds = input.req.getUserIds();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        boolean isAdmin = true; //TODO [ADMIN_CHECK}

        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId()
        );

        checks.assertRoomNotConfirmed(roomId);

        boolean forceExit = input.req.getForceExit() == null ? false : input.req.getForceExit() && isAdmin;
        boolean force = input.req.getForce() == null ? false : input.req.getForce() && isAdmin;

        for (Long targetUserId : targetUserIds) {
            // User checks
            checks.assertUserHasOrderAndItsNotDaily(targetUserId, event);
            checks.assertUserDoesNotOwnAroom(targetUserId, event);
            checks.assertUserIsNotRoomOwner(targetUserId, roomId);
            checks.assertUserIsNotInvitedToRoom(targetUserId, roomId);
            checks.assertOrderIsPaid(targetUserId, event);

            if (!forceExit) {
                checks.assertUserIsNotInRoom(targetUserId, event, false);
            }

            long guestId = roomLogic.invitePersonToRoom(targetUserId, roomId, event, force, forceExit);
            toReturn.add(new RoomGuest(guestId, targetUserId, roomId, false));
        }

        return new InviteToRoomResponse(toReturn);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull InviteToRoomRequest req,
            @NotNull Event event
    ) {}
}
