package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.RoomGeneralSanityCheck;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-generic-spot")
public class UserBuysGenericSpot implements RoomLogic {
    @NotNull private final RoomChecks checks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final RoomGeneralSanityCheck sanityChecks;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;

    @NotNull private final UpdatePretixPositionAction updatePretixPositionAction;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        return r.isPresent() && !r.get() && defaultRoomLogic.canCreateRoom(userId, event);
    }

    @Override
    public long createRoom(String name, long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        checks.assertUserHasNotBoughtAroom(userId, event);
        return defaultRoomLogic.createRoom(name, userId, event, pretixInformation);
    }

    @Override
    public boolean deleteRoom(long roomId) {
        return defaultRoomLogic.deleteRoom(roomId);
    }

    @Override
    public boolean changeRoomName(String name, long roomId) {
        return defaultRoomLogic.changeRoomName(name, roomId);
    }

    @Override
    public boolean setShowInNosecount(boolean showInNosecount, long roomId) {
        return defaultRoomLogic.setShowInNosecount(showInNosecount, roomId);
    }

    @Override
    public long invitePersonToRoom(long invitedUserId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation, boolean force, boolean forceExit) {
        checks.assertRoomLessThenBiggestCapacity(roomId, true, pretixInformation);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.invitePersonToRoom(invitedUserId, roomId, event, pretixInformation, force, forceExit);
    }

    @Override
    public boolean inviteAccept(long guestId, long invitedUserId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        checks.assertRoomLessThenBiggestCapacity(roomId, true, pretixInformation);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.inviteAccept(guestId, invitedUserId, roomId, event, pretixInformation);
    }

    @Override
    public boolean inviteRefuse(long guestId) {
        return defaultRoomLogic.inviteRefuse(guestId);
    }

    @Override
    public boolean inviteCancel(long guestId) {
        return defaultRoomLogic.inviteCancel(guestId);
    }

    @Override
    public boolean kickFromRoom(long guestId) {
        return defaultRoomLogic.kickFromRoom(guestId);
    }

    @Override
    public boolean leaveRoom(long guestId) {
        return defaultRoomLogic.leaveRoom(guestId);
    }

    @Override
    public boolean isConfirmationSupported() {
        return true;
    }

    @Override
    public boolean canConfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        List<RoomGuest> roomMates = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        List<Long> possibleRoomItemIds = pretixInformation.getRoomItemIdsForCapacity((short) roomMates.size());

        if (possibleRoomItemIds.isEmpty()) {
            log.error("[ROOM_CONFIRMATION] Unable to fetch a possible room item for the current room capacity ({})", roomMates.size());
            return false;
        }

        long roomItemId = possibleRoomItemIds.getFirst();
        var q = pretixInformation.getSmallestAvailabilityFromItemId(roomItemId);
        if (!q.isPresent()) {
            log.error("[ROOM_CONFIRMATION] Confirming room {} on event {}: Unable to fetch quota of item {}", roomId, event, roomItemId);
            return false;
        }
        if (!q.get().isAvailable()) {
            return false;
        }

        long ownerId = checks.getOwnerFromRoomIdAndAssertItExists(roomId);
        checks.assertUserHasNotBoughtAroom(ownerId, event);
        return defaultRoomLogic.canConfirmRoom(roomId, event, pretixInformation);
    }

    @Override
    public boolean confirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.error("[ROOM_CONFIRMATION] Confirming room {} on event {}", roomId, event);
        long ownerId = checks.getOwnerFromRoomIdAndAssertItExists(roomId);
        checks.assertUserHasNotBoughtAroom(ownerId, event);
        if (!defaultRoomLogic.canConfirmRoom(roomId, event, pretixInformation)) {
            log.error("[ROOM_CONFIRMATION] defaultRoomLogic.canConfirmRoom() returned false on roomId {}", roomId);
            return false;
        }

        List<RoomGuest> roomMates = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        short roomCapacity  = (short) roomMates.size();
        List<Long> possibleRoomItemIds = pretixInformation.getRoomItemIdsForCapacity(roomCapacity);
        if (possibleRoomItemIds.isEmpty()) {
            log.error("[ROOM_CONFIRMATION] Unable to fetch a possible room item for the current room capacity ({}). RoomId = {}", roomCapacity, roomId);
            return false;
        }
        long roomItemId = possibleRoomItemIds.getFirst();

        Order order = generalChecks.getOrderAndAssertItExists(ownerId, event, pretixInformation);
        long positionId = Objects.requireNonNull(order.getRoomPositionId());

        //Quota check is handled automatically by pretix when updating the item
        PretixPosition position = updatePretixPositionAction.invoke(event, positionId, new UpdatePretixPositionRequest(
                order.getCode(),
                roomItemId,
                0L
        ));
        if (position == null) {
            log.error("[ROOM_CONFIRMATION] Room {} with capacity {}: An error occurred while updating room position to order {}",
                    roomId, roomCapacity, order);
            return false;
        }

        var orgEvt = event.getOrganizerAndEventPair();
        var pretixOrder = pretixOrderFinder.fetchOrderByCode(orgEvt.getOrganizer(), orgEvt.getEvent(), order.getCode());
        if (!pretixOrder.isPresent()) {
            log.error("[ROOM_CONFIRMATION] Room {} with capacity {}: Unable to refetch order {} from pretix",
                    roomId, roomCapacity, order.getCode());
        } else {
            boolean updateSuccess = updateOrderInDb.execute(pretixOrder.get(), event, pretixInformation).isPresent();
            if (!updateSuccess) {
                log.error("[ROOM_CONFIRMATION] Room {} with capacity {}: Unable to parse order {} from pretix",
                        roomId, roomCapacity, order.getCode());
            }
        }

        return defaultRoomLogic.confirmRoom(roomId, event, pretixInformation);
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return true;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        long ownerId = checks.getOwnerFromRoomIdAndAssertItExists(roomId);
        checks.assertUserHasBoughtAroom(ownerId, event);
        return defaultRoomLogic.canUnconfirmRoom(roomId, event, pretixInformation);
    }

    @Override
    public boolean unconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.error("[ROOM_UNCONFIRMATION] Unconfirming room {} on event {}", roomId, event);
        long ownerId = checks.getOwnerFromRoomIdAndAssertItExists(roomId);
        checks.assertUserHasBoughtAroom(ownerId, event);
        if (!defaultRoomLogic.canUnconfirmRoom(roomId, event, pretixInformation)) {
            log.error("[ROOM_UNCONFIRMATION] defaultRoomLogic.canUnconfirmRoom() returned false on roomId {}", roomId);
            return false;
        }

        long noRoomItemId = (long) pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];

        Order order = generalChecks.getOrderAndAssertItExists(ownerId, event, pretixInformation);
        long positionId = Objects.requireNonNull(order.getRoomPositionId());

        PretixPosition position = updatePretixPositionAction.invoke(event, positionId, new UpdatePretixPositionRequest(
                order.getCode(),
                noRoomItemId,
                0L
        ));
        if (position == null) {
            log.error("[ROOM_UNCONFIRMATION] Room {}: An error occurred while updating room position to order {}",
                    roomId, order);
            return false;
        }

        var orgEvt = event.getOrganizerAndEventPair();
        var pretixOrder = pretixOrderFinder.fetchOrderByCode(orgEvt.getOrganizer(), orgEvt.getEvent(), order.getCode());
        if (!pretixOrder.isPresent()) {
            log.error("[ROOM_UNCONFIRMATION] Room {}: Unable to refetch order {} from pretix",
                    roomId, order.getCode());
        } else {
            boolean updateSuccess = updateOrderInDb.execute(pretixOrder.get(), event, pretixInformation).isPresent();
            if (!updateSuccess) {
                log.error("[ROOM_UNCONFIRMATION] Room {}: Unable to parse order {} from pretix",
                        roomId, order.getCode());
            }
        }

        return defaultRoomLogic.unconfirmRoom(roomId, event, pretixInformation);
    }

    @Override
    public boolean isExchangeRoomSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean isExchangeFullOrderSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, @Nullable Long targetRoomId, @Nullable Long sourceRoomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        //TODO implement when pretix is updated
        log.warn("Exchange of room not supported with UserBuysGenericSpot");
        return false;
    }
    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        //TODO implement when pretix is updated
        log.warn("Exchange of full orders not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public boolean isRefundRoomSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean isRefundFullOrderSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund room not supported with UserBuysGenericSpot");
        return false;
    }
    @Override
    public boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund full order not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public boolean isRoomBuyOrUpgradeSupported(@NotNull Event even) {
        return false;
    }
    @Override
    public boolean buyOrUpgradeRoom(long newRoomItemId, long newRoomPrice, @Nullable Long oldRoomPaid, long userId, @Nullable Long roomId, @Nullable Long newEarlyItemId, @Nullable Long newEarlyPrice, @Nullable Long oldEarlyPaid, @Nullable Long newLateItemId, @Nullable Long newLatePrice, @Nullable Long oldLatePaid, @NotNull Order order, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Buy or upgrade room not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public @Nullable ExtraDays getExtraDaysForUser(long userId, long eventId) {
        return orderFinder.getExtraDaysOfUser(userId, eventId);
    }

    @Override
    public void computeNosecountExtraDays(@NotNull NosecountRoom room) {
    }

    @Override
    public void updateRoomCapacity(@NotNull RoomData roomData, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        if (roomData.getRoomCapacity() <= 0) {
            roomData.setRoomCapacity(Objects.requireNonNull(pretixInformation.getBiggestRoomCapacity()).capacity());
        }
    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation, @Nullable List<String> detectedErrors) {
        sanityChecks.doSanityChecks(
                roomId,
                this,
                pretixInformation,
                detectedErrors
        );
    }
}
