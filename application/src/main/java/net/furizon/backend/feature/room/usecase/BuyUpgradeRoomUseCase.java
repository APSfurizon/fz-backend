package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.request.BuyUpgradeRoomRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
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

        ExtraDays extraDays = order.getExtraDays();
        HotelCapacityPair newRoomInfo = pretixInformation.getRoomInfoFromPretixItemId(newRoomItemId);
        if (newRoomInfo == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {}: Unable to fetch capacity of new room",
                    userId, newRoomItemId);
            return false;
        }

        //Get new room price
        Long newRoomPrice = pretixInformation.getItemPrice(newRoomItemId, true);
        if (newRoomPrice == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {}: Unable to fetch price of new room",
                    userId, newRoomItemId);
            return false;
        }
        long newRoomExtraDaysPrice = 0L;
        if (extraDays.isEarly()) {
            long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(newRoomInfo, ExtraDays.EARLY));
            newRoomExtraDaysPrice += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
        }
        if (extraDays.isLate()) {
            long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(newRoomInfo, ExtraDays.LATE));
            newRoomExtraDaysPrice += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
        }
        long newRoomTotal = newRoomPrice + newRoomExtraDaysPrice;

        //Get old room paid
        Optional<PretixPosition> oldRoomPosition = oldRoomPositionId == null ? Optional.empty() : positionFinder.fetchPositionById(event, oldRoomPositionId);
        long oldRoomPaid = oldRoomPosition.map(p -> PretixGenericUtils.fromStrPriceToLong(p.getPrice())).orElse(0L);
        //TODO find extra days positions
        if (oldRoomPaid > newRoomTotal) {
            log.error("[ROOM_BUY] User {} buying roomItemId {}: Selected room costs less than what was already paid ({} < {})",
                    userId, newRoomItemId, newRoomPrice, oldRoomPaid);
            throw new ApiException("New room costs less than what paid!", RoomErrorCodes.BUY_ROOM_NEW_ROOM_COSTS_LESS);
        }

        //Check room capacity
        List<RoomGuest> guests = oldRoomId == null ? null : roomFinder.getRoomGuestsFromRoomId(oldRoomId, true);
        if (guests != null && guests.size() > newRoomInfo.capacity()) {
            log.error("[ROOM_BUY] User {} buying roomItemId {}: New room has capacity of {}, but {} were already present in the room",
                    userId, newRoomItemId, newRoomInfo.capacity(), guests.size());
            throw new ApiException("New room is too small!", RoomErrorCodes.BUY_ROOM_NEW_ROOM_LOW_CAPACITY);
        }

        return roomLogic.buyOrUpgradeRoom(newRoomItemId, userId, oldRoomId, order, event, pretixInformation);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull BuyUpgradeRoomRequest req,
            @NotNull PretixInformation pretixInformation
    ) {}
}
