package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.request.BuyUpgradeRoomRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyUpgradeRoomUseCase implements UseCase<BuyUpgradeRoomUseCase.Input, Boolean> {
    @NotNull private final PretixPositionFinder positionFinder;
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

        long newRoomItemId = input.req.getRoomPretixItemId();
        Long oldRoomPositionId = order.getRoomPositionId();
        Long oldRoomId = null;
        if (oldRoomPositionId != null) {
            //User may have NO_ROOM item so we have to do this double check
            if (order.hasRoom()) {
                var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
                if (r.isPresent()) {
                    oldRoomId = r.get();
                    checks.assertRoomNotConfirmed(oldRoomId);
                }
            }
        }

        //Check room price
        Long newRoomPrice = pretixInformation.getRoomPriceByItemId(newRoomItemId, true);
        if (newRoomPrice == null) {
            //TODO unable to find new room price
            return false;
        }
        Optional<PretixPosition> oldRoomPosition = oldRoomPositionId == null ? Optional.empty() : positionFinder.fetchPositionById(event, oldRoomPositionId);
        long oldRoomPaid = oldRoomPosition.map(p -> PretixGenericUtils.fromStrPriceToLong(p.getPrice())).orElse(0L);
        if (oldRoomPaid > newRoomPrice) {
            log.error("");
            throw new ApiException(""); //TODO new room costs less than what already paid
        }

        //Check room capacity
        List<RoomGuest> guests = oldRoomId == null ? null : roomFinder.getRoomGuestsFromRoomId(oldRoomId, true);
        HotelCapacityPair newRoomInfo = pretixInformation.getRoomInfoFromPretixItemId(newRoomItemId);
        if (newRoomInfo == null) {
            //TODO unable to get new room capacity
            return false;
        }
        if (guests != null && guests.size() > newRoomInfo.capacity()) {
            log.error("");
            throw new ApiException(""); //TODO unable to fit all guests in new room
        }

        return roomLogic.buyOrUpgradeRoom(newRoomItemId, userId, oldRoomId, order, event, pretixInformation);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull BuyUpgradeRoomRequest req,
            @NotNull PretixInformation pretixInformation
    ) {}
}
