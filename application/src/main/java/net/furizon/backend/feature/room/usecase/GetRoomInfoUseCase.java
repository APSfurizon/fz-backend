package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetRoomInfoUseCase implements UseCase<GetRoomInfoUseCase.Input, RoomInfoResponse> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;

    @Override
    public @NotNull RoomInfoResponse executor(@NotNull GetRoomInfoUseCase.Input input) {
        long userId = input.user.getUserId();
        RoomInfo info = roomFinder.getRoomInfoForUser(userId, input.event, input.pretixInformation);

        if (info != null) {
            boolean isOwner = info.getRoomOwnerId() == userId;
            long roomId = info.getRoomId();
            info.setOwner(isOwner);
            info.setCanConfirm(isOwner && !info.isConfirmed() && roomLogic.canConfirmRoom(roomId, input.event));
            info.setCanUnconfirm(isOwner && info.isConfirmed() && roomLogic.canUnconfirmRoom(roomId));

            List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, input.event);

            info.setCanInvite(isOwner && (
                    guests.stream().filter(g -> g.getRoomGuest().isConfirmed()).count()
                    < (long) info.getRoomData().getRoomCapacity()
                )
            );
            info.setGuests(guests);
        }
        List<RoomInvitationResponse> invitations =
            roomFinder.getUserReceivedInvitations(userId, input.event, input.pretixInformation);

        boolean canCreateRoom = info == null && roomLogic.canCreateRoom(userId, input.event);

        return new RoomInfoResponse(info, canCreateRoom, invitations);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
