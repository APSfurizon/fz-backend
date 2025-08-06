package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.request.BuyUpgradeRoomRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_TYPE_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.LANG_PRETIX;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_WAS_UPGRADED;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyUpgradeRoomUseCase implements UseCase<BuyUpgradeRoomUseCase.Input, Boolean> {
    @NotNull private final PretixPositionFinder positionFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull BuyUpgradeRoomUseCase.Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long userId = roomChecks.getUserIdAssertPermissionCheckTimeframe(input.req.getUserId(), input.user);
        Order order = generalChecks.getOrderAndAssertItExists(userId, event, pretixInformation);

        generalChecks.assertOrderIsPaid(order, userId, event);
        generalChecks.assertPaymentAndRefundConfirmed(order.getCode(), event);
        generalChecks.assertOrderIsNotDaily(order, userId, event);
        roomChecks.assertUserIsNotInRoom(userId, event, true);

        long newRoomItemId = input.req.getRoomPretixItemId();
        Long earlyPositionId = null;
        Long latePositionId = null;
        Long oldRoomItemId = order.getPretixRoomItemId();
        Long oldRoomPositionId = order.getRoomPositionId();
        Long oldRoomId = null;
        if (Objects.equals(oldRoomItemId, newRoomItemId)) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Selected room is the one he already owned!",
                userId, newRoomItemId, event);
            throw new ApiException(translationService.error("room.upgrade.fail.same_room"),
                RoomErrorCodes.BUY_ROOM_SAME_ROOM);
        }
        if (oldRoomPositionId != null) {
            //User may have NO_ROOM item so we have to do this double check
            if (order.hasRoom()) {
                var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
                if (r.isPresent()) {
                    oldRoomId = r.get();
                    roomChecks.assertRoomNotConfirmed(oldRoomId);
                }
                earlyPositionId = order.getEarlyPositionId();
                latePositionId = order.getLatePositionId();
            }
        }

        ExtraDays extraDays = order.getExtraDays();
        HotelCapacityPair newRoomInfo = pretixInformation.getRoomInfoFromPretixItemId(newRoomItemId);
        if (newRoomInfo == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to fetch capacity of new room",
                    userId, newRoomItemId, event);
            return false;
        }

        //Get new room price
        Long newRoomPrice = pretixInformation.getItemPrice(newRoomItemId, true, true);
        if (newRoomPrice == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to fetch price of new room",
                    userId, newRoomItemId, event);
            return false;
        }
        long newRoomEarlyPrice = 0L;
        long newRoomLatePrice = 0L;
        Long newEarlyItemId = null;
        Long newLateItemId = null;
        if (extraDays.isEarly()) {
            newEarlyItemId = Objects.requireNonNull(
                    pretixInformation.getExtraDayItemIdForHotelCapacity(newRoomInfo, ExtraDays.EARLY));
            newRoomEarlyPrice = Objects.requireNonNull(
                    pretixInformation.getItemPrice(newEarlyItemId, true, true));
        }
        if (extraDays.isLate()) {
            newLateItemId = Objects.requireNonNull(
                    pretixInformation.getExtraDayItemIdForHotelCapacity(newRoomInfo, ExtraDays.LATE));
            newRoomLatePrice = Objects.requireNonNull(
                    pretixInformation.getItemPrice(newLateItemId, true, true));
        }
        long newRoomTotal = newRoomPrice + newRoomEarlyPrice + newRoomLatePrice;

        //Get old room paid
        long oldRoomPaid = getPaid(oldRoomPositionId, event);
        long earlyPaid = getPaid(earlyPositionId, event);
        long latePaid = getPaid(latePositionId, event);
        long totalPaid = oldRoomPaid + earlyPaid + latePaid;
        if (totalPaid > newRoomTotal) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                + "Selected room costs less than what was already paid ({} < {})",
                userId, newRoomItemId, event, newRoomPrice, oldRoomPaid);
            throw new ApiException(translationService.error("room.upgrade.fail.price_change_negative"),
                RoomErrorCodes.BUY_ROOM_NEW_ROOM_COSTS_LESS);
        }

        //Check room capacity
        List<RoomGuest> guests = oldRoomId == null ? null : roomFinder.getRoomGuestsFromRoomId(oldRoomId, true);
        if (guests != null && guests.size() > newRoomInfo.capacity()) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                + "New room has capacity of {}, but {} were already present in the room",
                userId, newRoomItemId, event, newRoomInfo.capacity(), guests.size());
            throw new ApiException(translationService.error("room.upgrade.fail.too_small"),
                RoomErrorCodes.BUY_ROOM_NEW_ROOM_LOW_CAPACITY);
        }

        boolean res = roomLogic.buyOrUpgradeRoom(newRoomItemId, newRoomPrice, oldRoomPaid, userId, oldRoomId,
                newEarlyItemId, newRoomEarlyPrice, earlyPaid, newLateItemId,
                newRoomLatePrice, latePaid, order, event, pretixInformation);
        if (res && oldRoomId != null) {
            Map<String, String> names = pretixInformation.getRoomNamesFromRoomPretixItemId(newRoomItemId);
            if (names != null) {
                mailService.prepareAndSendBroadcastUpdate(
                        oldRoomId, TEMPLATE_ROOM_WAS_UPGRADED,
                        MailVarPair.of(ROOM_TYPE_NAME, names.get(LANG_PRETIX))
                );
            }
        }
        return res;
    }

    private long getPaid(@Nullable Long positionId, @NotNull Event event) {
        Optional<PretixPosition> position = positionId == null
                ? Optional.empty()
                : positionFinder.fetchPositionById(event, positionId);
        return position.map(p -> PretixGenericUtils.fromStrPriceToLong(p.getPrice())).orElse(0L);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull BuyUpgradeRoomRequest req,
            @NotNull PretixInformation pretixInformation
    ) {}
}
