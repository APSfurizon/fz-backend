package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.request.BuyUpgradeRoomRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyUpgradeRoomUseCase implements UseCase<BuyUpgradeRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull BuyUpgradeRoomUseCase.Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        checks.assertInTimeframeToEditRooms();
        long userId = checks.getUserIdAndAssertPermission(input.req.getUserId(), input.user);
        Order order = checks.getOrderAndAssertItExists(userId, event, pretixInformation);

        checks.assertOrderIsPaid(order, userId, event);
        checks.assertOrderIsNotDaily(order, userId, event);
        checks.assertUserIsNotInRoom(userId, event, true);

        Long roomId = null;
        long newRoomItemId = input.req.getRoomPretixItemId();
        Long oldRoomItemId = order.getPretixRoomItemId();
        if (oldRoomItemId != null) {
            //User may have NO_ROOM item so we have to do this double check
            if (order.hasRoom()) {
                var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
                if (r.isPresent()) {
                    roomId = r.get();
                    checks.assertRoomNotConfirmed(roomId);
                }
            }
        }

        //Fetch user's room price and guests
        Long newRoomPrice = pretixInformation.getRoomPriceByItemId(newRoomItemId, true);
        Long currentRoomPrice = oldRoomItemId == null ? null : pretixInformation.getRoomPriceByItemId(oldRoomItemId, true); //TODO we should get positions!
        List<RoomGuest> guests = roomId == null ? null : roomFinder.getRoomGuestsFromRoomId(roomId, true);

        //TODO Check prices and room members

        return null;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull BuyUpgradeRoomRequest req,
            @NotNull PretixInformation pretixInformation
    ) {}
}
