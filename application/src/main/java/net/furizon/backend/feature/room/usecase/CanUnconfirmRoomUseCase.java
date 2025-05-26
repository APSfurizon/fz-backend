package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.RoomChecks;
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
public class CanUnconfirmRoomUseCase implements UseCase<CanUnconfirmRoomUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull CanUnconfirmRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        long roomId = checks.getRoomIdAssertPermissionCheckTimeframe(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomConfirmed(roomId);
        checks.assertRoomFromCurrentEvent(roomId, event);

        return roomLogic.canUnconfirmRoom(roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable RoomIdRequest roomReq,
            @NotNull Event event
    ) {}
}
