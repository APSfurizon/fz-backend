package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeFullOrderUseCase implements UseCase<ExchangeFullOrderUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    //IMPORTANT: This useCase doesn't care about the confirmation flow. It should be done prior to this call!
    @Override
    public @NotNull Boolean executor(@NotNull ExchangeFullOrderUseCase.Input input) {
        log.info("[ROOM_EXCHANGE] User {} is trying a full order exchange", input.user.getUserId());
        long sourceUserId = checks.getUserIdAndAssertPermission(input.req.getSourceUserId(), input.user);
        long destUserId = input.req.getDestUserId();
        Event event = input.pretixInformation.getCurrentEvent();


        checks.assertInTimeframeToEditRooms();

        checks.assertUserHasOrder(sourceUserId, event);
        checks.assertUserHasNotAnOrder(destUserId, event);

        long roomId = -1L;
        var srcRoomId = roomFinder.getRoomIdFromOwnerUserId(sourceUserId, event);
        if (srcRoomId.isPresent()) {
            roomId = checks.getRoomIdAndAssertPermissionsOnRoom(sourceUserId, event, null);
            checks.assertRoomNotConfirmed(roomId);
        }

        checks.assertOrderIsPaid(sourceUserId, event);

        if (input.runOnlyChecks) {
            return true;
        }

        //TODO EMAIL alert everyone that the room has changed owner
        return roomLogic.exchangeFullOrder(destUserId, sourceUserId, roomId, event, input.pretixInformation);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ExchangeRequest req,
            @NotNull PretixInformation pretixInformation,
            boolean runOnlyChecks
    ) {}
}
