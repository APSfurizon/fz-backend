package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.InviteToRoomResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
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
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;


    @Override
    @Transactional
    public @NotNull InviteToRoomResponse executor(@NotNull Input input) {
        Set<RoomGuest> toReturn = new HashSet<>();
        long requesterUserId = input.user.getUserId();
        Set<Long> targetUserIds = input.req.getUserIds();
        Event event = input.event;

        boolean isAdmin = roomChecks.isUserAdmin(requesterUserId);
        roomChecks.assertInTimeframeToEditRoomsAllowAdmin(requesterUserId, input.req.getRoomId(), isAdmin);
        long roomId = roomChecks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId(),
                isAdmin
        );

        roomChecks.assertRoomNotConfirmed(roomId);
        roomChecks.assertRoomFromCurrentEvent(roomId, event);

        boolean forceExit = input.req.getForceExit() == null ? false : input.req.getForceExit() && isAdmin;
        boolean force = input.req.getForce() == null ? false : input.req.getForce() && isAdmin;

        for (Long targetUserId : targetUserIds) {
            // User checks
            generalChecks.assertUserHasOrderAndItsNotDaily(targetUserId, event);
            roomChecks.assertUserDoesNotOwnAroom(targetUserId, event);
            roomChecks.assertUserIsNotRoomOwner(targetUserId, roomId);
            roomChecks.assertUserIsNotInvitedToRoom(targetUserId, roomId);
            generalChecks.assertOrderIsPaid(targetUserId, event);

            if (!forceExit) {
                roomChecks.assertUserIsNotInRoom(targetUserId, event, false);
            }

            long guestId = roomLogic.invitePersonToRoom(
                    targetUserId, roomId,
                    event, input.pretixInformation,
                    force, forceExit
            );
            toReturn.add(new RoomGuest(guestId, targetUserId, roomId, false));
        }

        return new InviteToRoomResponse(toReturn);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull InviteToRoomRequest req,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
