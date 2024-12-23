package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmRoomUseCase implements UseCase<ConfirmRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull ConfirmRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomNotConfirmed(roomId);
        checks.assertRoomCanBeConfirmed(roomId, event, roomLogic);
        roomFinder.getRoomGuestsFromRoomId(roomId, true).forEach(g -> checks.assertOrderIsPaid(g.getUserId(), event));
        //TODO run sanity checks

        return roomLogic.confirmRoom(roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable RoomIdRequest roomReq,
            @NotNull Event event
    ) {}
}
