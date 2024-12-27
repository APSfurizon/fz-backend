package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomChecks {
    @NotNull private final ExchangeConfirmationFinder exchangeConfirmationFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomConfig roomConfig;

    public void assertInTimeframeToEditRooms() {
        OffsetDateTime end = roomConfig.getRoomChangesEndTime();
        if (end != null && end.isBefore(OffsetDateTime.now())) {
            log.error("Editing of rooms is disabled after the date {}", end);
            throw new ApiException("Room editing timeframe has ended", RoomErrorCodes.EDIT_TIMEFRAME_ENDED);
        }
    }

    @NotNull public Order getOrderAndAssertItExists(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        Order order = orderFinder.findOrderByUserIdEvent(userId, event, pretixInformation);
        assertOrderFound(Optional.ofNullable(order), userId, event);
        return order;
    }
    public void assertUserHasOrder(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        assertOrderFound(isDaily, userId, event);
    }
    public void assertUserHasOrderAndItsNotDaily(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        assertOrderFound(isDaily, userId, event);
        assertOrderIsNotDailyPrint(isDaily.get(), userId, event);
    }
    public void assertOrderIsNotDaily(@NotNull Order order, long userId, @NotNull Event event) {
        assertOrderIsNotDailyPrint(order.isDaily(), userId, event);
    }
    public void assertUserHasNotAnOrder(long userId, @NotNull Event event) {
        var r = orderFinder.isOrderDaily(userId, event);
        if (r.isPresent()) {
            log.error("User {} has bought an order for event {}!", userId, event);
            throw new ApiException("User has already bought an order", RoomErrorCodes.ORDER_ALREADY_BOUGHT);
        }
    }

    public void assertUserHasBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        assertOrderFound(r, userId, event);
        if (!r.get()) {
            log.error("User {} doesn't have purchased a room for event {}", userId, event);
            throw new ApiException("User hasn't purchased a room", RoomErrorCodes.USER_HAS_NOT_PURCHASED_A_ROOM);
        }
    }
    public void assertUserHasNotBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        assertOrderFound(r, userId, event);
        if (r.get()) {
            log.error("User {} have purchased a room for event {} and cannot join another room", userId, event);
            throw new ApiException("User has already purchased a room", RoomErrorCodes.USER_HAS_PURCHASED_A_ROOM);
        }
    }

    public void assertUserIsNotInRoom(long userId, @NotNull Event event, boolean ownRoomAllowed) {
        boolean userInRoom = roomFinder.isUserInAroom(userId, event.getId(), ownRoomAllowed);
        if (userInRoom) {
            log.error("User {} is trying to manage a room for event {}, but he's already in one!", userId, event);
            throw new ApiException("User already is in a room", RoomErrorCodes.USER_ALREADY_IS_IN_A_ROOM);
        }
    }
    public void assertUserIsNotInvitedToRoom(long userId, long roomId) {
        boolean userInRoom = roomFinder.isUserInvitedInRoom(userId, roomId);
        if (userInRoom) {
            log.error("User {} is already invited to room {}", userId, roomId);
            throw new ApiException("User is already invited to specified room", RoomErrorCodes.USER_ALREADY_INVITED_TO_ROOM);
        }
    }

    public void assertUserDoesNotOwnAroom(long userId, @NotNull Event event) {
        boolean alreadyOwnsAroom = roomFinder.userOwnsAroom(userId, event.getId());
        if (alreadyOwnsAroom) {
            log.error("User {} is trying to manage a room for event {}, but already has one!", userId, event);
            throw new ApiException("User already owns a room", RoomErrorCodes.USER_OWNS_A_ROOM);
        }
    }
    public void assertUserIsNotRoomOwner(long userId, long roomId) {
        var roomOwner = roomFinder.getOwnerUserIdFromRoomId(roomId);
        assertRoomFound(roomOwner, roomId);
        if (roomOwner.get() == userId) {
            log.error("User {} is the owner of the room {}!", userId, roomId);
            throw new ApiException("User is the owner of the room", RoomErrorCodes.USER_IS_OWNER_OF_ROOM);
        }
    }
    public long getRoomIdAndAssertPermissionsOnRoom(long userId, @NotNull Event event, @Nullable Long roomReqId) {
        var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
        long roomId;

        if (roomReqId == null || roomReqId < 0L) {
            if (r.isPresent()) {
                roomId = r.get();
            } else {
                log.error("User doesn't own a room!");
                throw new ApiException("User doesn't own a room", RoomErrorCodes.USER_DOES_NOT_OWN_A_ROOM);
            }
        } else {
            long rId = roomReqId;
            if (!r.isPresent() || r.get() != rId) {
                if (true) { //TODO [ADMIN_CHECK]
                    roomId = rId;
                    if (!roomFinder.isRoomConfirmed(roomId).isPresent()) {
                        log.error("Room with id {} doesn't exist!", roomId);
                        throw new ApiException("Room not found", RoomErrorCodes.ROOM_NOT_FOUND);
                    }
                } else {
                    log.error("User is not an admin! It cannot operate on room {}", rId);
                    throw new ApiException("User is not an admin!", RoomErrorCodes.USER_IS_NOT_ADMIN);
                }
            } else {
                roomId = rId;
            }
        }

        return roomId;
    }

    public void assertRoomNotConfirmed(long roomId) {
        var r = roomFinder.isRoomConfirmed(roomId);
        assertRoomFound(r, roomId);
        if (r.get()) {
            log.error("Room {} is already confirmed!", roomId);
            throw new ApiException("Room is already confirmed", RoomErrorCodes.ROOM_ALREADY_CONFIRMED);
        }
    }
    public void assertRoomConfirmed(long roomId) {
        var r = roomFinder.isRoomConfirmed(roomId);
        assertRoomFound(r, roomId);
        if (!r.get()) {
            log.error("Room {} is NOT confirmed!", roomId);
            throw new ApiException("Room is NOT confirmed", RoomErrorCodes.ROOM_NOT_CONFIRMED);
        }
    }

    public void assertRoomCanBeConfirmed(long roomId, @NotNull Event event, @NotNull RoomLogic logic) {
        if (!logic.canConfirmRoom(roomId, event)) {
            log.error("Room {} cannot be confirmed!", roomId);
            throw new ApiException("Room cannot be confirmed", RoomErrorCodes.ROOM_CANNOT_BE_CONFIRMED);
        }
    }
    public void assertRoomCanBeUnconfirmed(long roomId, @NotNull RoomLogic logic) {
        if (!logic.canUnconfirmRoom(roomId)) {
            log.error("Room {} cannot be unconfirmed!", roomId);
            throw new ApiException("Room cannot be unconfirmed", RoomErrorCodes.ROOM_CANNOT_BE_UNCONFIRMED);
        }
    }

    @NotNull public RoomGuest getRoomGuestObjFromUserEventAndAssertItExistsAndConfirmed(long userId, @NotNull Event event) {
        var r = roomFinder.getConfirmedRoomGuestFromUserEvent(userId, event);
        if (!r.isPresent()) {
            log.error("Could not find any roomGuest obj for user {} at event {}", userId, event);
            throw new ApiException("Could not find any roomGuest", RoomErrorCodes.GUEST_NOT_FOUND);
        }
        return r.get();
    }
    @NotNull public RoomGuest getRoomGuestObjAndAssertItExists(long guestId) {
        var r = roomFinder.getRoomGuestFromId(guestId);
        if (!r.isPresent()) {
            log.error("Unable to find roomGuest for id {}", guestId);
            throw new ApiException("Unable to find guest", RoomErrorCodes.GUEST_NOT_FOUND);
        }
        return r.get();
    }
    public void assertGuestIsNotConfirmed(RoomGuest guest) {
        if (guest.isConfirmed()) {
            log.error("Guest {} is already confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is already confirmed!", RoomErrorCodes.GUEST_ALREADY_CONFIRMED);
        }
    }
    public void assertGuestIsConfirmed(RoomGuest guest) {
        if (!guest.isConfirmed()) {
            log.error("Guest {} is NOT confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is NOT confirmed!", RoomErrorCodes.GUEST_NOT_CONFIRMED);

        }
    }

    public void assertIsGuestObjOwnerOrAdmin(RoomGuest guest, long requesterUserId) {
        boolean isAdmin = true; //TODO [ADMIN_CHECK]
        if (guest.getUserId() != requesterUserId && !isAdmin) {
            log.error("User {} has no rights over guest obj {}", requesterUserId, guest.getGuestId());
            throw new ApiException("User has no rights over specified guest!", RoomErrorCodes.USER_IS_NOT_ADMIN);
        }
    }

    public void assertRoomNotFull(long roomId, boolean onlyConfirmed) {
        List<RoomGuest> roomMates = roomFinder.getRoomGuestsFromRoomId(roomId, onlyConfirmed);
        Optional<Short> capacity = roomFinder.getRoomCapacity(roomId);

        if (!capacity.isPresent()) {
            log.error("Room {} not found while checking capacity", roomId);
            throw new ApiException("Room not found", RoomErrorCodes.ROOM_NOT_FOUND);
        }

        if (capacity.get() <= roomMates.size()) {
            log.error("Room {} is already full!", roomId);
            throw new ApiException("Room is full", RoomErrorCodes.ROOM_FULL);
        }
    }

    public void assertOrderIsPaid(long userId, @NotNull Event event) {
        var r = orderFinder.getOrderStatus(userId, event);
        assertOrderFound(r, userId, event);
        assertOrderStatusPaid(r.get(), userId, event);
    }
    public void assertOrderIsPaid(@NotNull Order order, long userId, @NotNull Event event) {
        assertOrderStatusPaid(order.getOrderStatus(), userId, event);
    }

    public void assertBothUsersHasConfirmed(long exchangeStatusId) {
        var r = exchangeConfirmationFinder.getExchangeStatusFromId(exchangeStatusId);
        if (r == null) {
            log.error("No confirmed exchange status found for exchangeId {}", exchangeStatusId);
            throw new ApiException("Exchange not found", RoomErrorCodes.EXCHANGE_NOT_FOUND);
        }
        if (!r.isFullyConfirmed()) {
            log.error("Exchange {} is not fully confirmed", exchangeStatusId);
            throw new ApiException("Exchange is not fully confirmed", RoomErrorCodes.EXCHANGE_NOT_FULLY_CONFIRMED);
        }
    }

    public long getUserIdAndAssertPermission(@Nullable Long userId, @NotNull FurizonUser user) {
        long id = userId == null ? user.getUserId() : userId;
        boolean isAdmin = true; //TODO [ADMIN_CHECK]
        if (userId != null && userId != user.getUserId() && !isAdmin) {
            log.error("User {} has no permission over userId {}", user.getUserId(), userId);
            throw new ApiException("User is not an admin", RoomErrorCodes.USER_IS_NOT_ADMIN);
        }
        return id;
    }

    private void assertOrderStatusPaid(@NotNull OrderStatus status, long userId, @NotNull Event event) {
        if (status != OrderStatus.PAID) {
            log.error("Order for user {} on event {} is not paid", userId, event);
            throw new ApiException("Order is not paid", RoomErrorCodes.ORDER_NOT_PAID);
        }
    }

    private void assertOrderIsNotDailyPrint(boolean isDaily, long userId, @NotNull Event event) {
        if (isDaily) {
            log.error("User {} is trying to manage a room on event {}, but has a daily ticket!", userId, event);
            throw new ApiException("User has a daily ticket", RoomErrorCodes.USER_HAS_DAILY_TICKET);
        }
    }

    private void assertOrderFound(Optional<?> r, long userId, @NotNull Event event) {
        if (!r.isPresent()) {
            log.error("No order found for user {} on event {}", userId, event);
            throw new ApiException("Order not found", RoomErrorCodes.ORDER_NOT_FOUND);
        }
    }

    private void assertRoomFound(Optional<?> o, long roomId) {
        if (!o.isPresent()) {
            log.error("Room {} was not found!", roomId);
            throw new ApiException("Room not found", RoomErrorCodes.ROOM_NOT_FOUND);
        }
    }
}
