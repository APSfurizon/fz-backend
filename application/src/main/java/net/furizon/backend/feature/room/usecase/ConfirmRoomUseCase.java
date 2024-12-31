package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmRoomUseCase implements UseCase<ConfirmRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    private final PretixInformation pretixInformation;

    @Override
    public @NotNull Boolean executor(@NotNull ConfirmRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        log.info("User {} is confirming a room", requesterUserId);
        Event event = input.event;

        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomNotConfirmed(roomId);
        checks.assertRoomCanBeConfirmed(roomId, event, roomLogic);
        roomFinder.getRoomGuestsFromRoomId(roomId, true).forEach(g -> checks.assertOrderIsPaid(g.getUserId(), event));

        List<String> errors = new LinkedList<>();
        roomLogic.doSanityChecks(roomId, pretixInformation, errors);
        if (errors.size() > 0) {
            log.error("Sanity checks failed while confirming room {}", roomId);
            return false;
        }

        //TODO EMAIL send email to everyone that room has been confirmed
        return roomLogic.confirmRoom(roomId);
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable RoomIdRequest roomReq,
            @NotNull Event event
    ) {}
}
