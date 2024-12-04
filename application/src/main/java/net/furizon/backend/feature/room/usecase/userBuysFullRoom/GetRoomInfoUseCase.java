package net.furizon.backend.feature.room.usecase.userBuysFullRoom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetRoomInfoUseCase implements UseCase<GetRoomInfoUseCase.Input, RoomInfoResponse> {
    @NotNull private final RoomFinder roomFinder;

    @Override
    public @NotNull RoomInfoResponse executor(@NotNull GetRoomInfoUseCase.Input input) {
        if (input.event == null) {
            log.error("Event is null");
            throw new RuntimeException("Event is null");
        }

        long userId = input.user.getUserId();
        List<RoomGuestResponse> invitations = null;
        RoomInfo info = roomFinder.getRoomInfoForUser(userId, input.event, input.pretixInformation);

        if (info == null) {
            invitations = roomFinder.getUserReceivedInvitations(userId, input.event);
        } else {
            info.setOwner(info.getRoomOwnerId() == userId);
            info.setGuests(roomFinder.getRoomGuests(info.getRoomId()));
        }

        return new RoomInfoResponse(info, invitations);
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
