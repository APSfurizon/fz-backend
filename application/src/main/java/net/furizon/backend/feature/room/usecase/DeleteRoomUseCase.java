package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteRoomUseCase implements UseCase<DeleteRoomUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomNotConfirmed(roomId);

        //TODO EMAIL send an email to everyone that room has been deleted
        return roomLogic.deleteRoom(roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable RoomIdRequest roomReq,
            @NotNull Event event
    ) {}
}
