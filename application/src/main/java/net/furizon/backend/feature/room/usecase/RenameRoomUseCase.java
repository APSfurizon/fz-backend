package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ChangeNameToRoomRequest;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RenameRoomUseCase implements UseCase<RenameRoomUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final CommonRoomChecks commonChecks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        long roomId = commonChecks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.req.getRoomId()
        );

        roomLogic.changeRoomName(input.req.getName(), roomId);

        return true;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ChangeNameToRoomRequest req,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
