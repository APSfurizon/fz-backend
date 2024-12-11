package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor

public class InviteToRoomUseCase implements UseCase<InviteToRoomUseCase.Input, RoomGuestResponse> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final CommonRoomChecks commonChecks;


    @Override
    public @NotNull RoomGuestResponse executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        long targetUserId = input.req.getUserId();
        Event event = input.event;

        commonChecks.runCommonChecks(targetUserId, event);

        long roomId = commonChecks.getAndCheckRoomId(
                requesterUserId,
                event,
                input.req.getRoomId()
        );

        boolean forceExit = input.req.getForceExit() == null ? false : input.req.getForceExit();
        if (!forceExit) {
            commonChecks.isUserInAroomCheck(targetUserId, event);
        }

        commonChecks.isRoomAlreadyConfirmedCheck(roomId);

        boolean force = input.req.getForce() == null ? false : input.req.getForce();
        long guestId = roomLogic.invitePersonToRoom(targetUserId, roomId, force, forceExit);

        return new RoomGuestResponse(guestId, targetUserId, roomId, false);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull InviteToRoomRequest req,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
