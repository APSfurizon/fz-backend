package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomChecks {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;

    public void assertUserHasOrderAndItsNotDaily(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        assertOrderFound(isDaily, userId, event);
        if (isDaily.get()) {
            log.error("User is trying to manage a room, but has a daily ticket!");
            throw new ApiException("User has a daily ticket");
        }
    }

    public void assertUserHasBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        assertOrderFound(r, userId, event);
        if (!r.get()) {
            log.error("User {} doesn't have purchased a room for event {}", userId, event);
            throw new ApiException("User hasn't purchased a room");
        }
    }
    public void assertUserHasNotBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        assertOrderFound(r, userId, event);
        if (r.get()) {
            log.error("User {} have purchased a room for event {} and cannot join another room", userId, event);
            throw new ApiException("User hasn't purchased a room");
        }
    }

    public void assertUserDoesNotOwnAroom(long userId, @NotNull Event event) {
        boolean alreadyOwnsAroom = roomFinder.userOwnsAroom(userId, event.getId());
        if (alreadyOwnsAroom) {
            log.error("User {} is trying to manage a room for event {}, but already has one!", userId, event);
            throw new ApiException("User already owns a room");
        }
    }

    public void assertUserIsNotInRoom(long userId, @NotNull Event event) {
        boolean userInRoom = roomFinder.isUserInAroom(userId, event.getId());
        if (userInRoom) {
            log.error("User {} is trying to manage a room for event {}, but he's already in one!", userId, event);
            throw new ApiException("User already is in a room");
        }
    }
    public void assertUserIsNotInvitedToRoom(long userId, long roomId) {
        boolean userInRoom = roomFinder.isUserInvitedInRoom(userId, roomId);
        if (userInRoom) {
            log.error("User {} is already invited to room {}", userId, roomId);
            throw new ApiException("User is already invited to specified room");
        }
    }

    public void assertUserIsNotRoomOwner(long userId, long roomId) {
        var roomOwner = roomFinder.getOwnerUserIdFromRoomId(roomId);
        assertRoomFound(roomOwner, roomId);
        if (roomOwner.get() == userId) {
            log.error("User {} is the owner of the room {}!", userId, roomId);
            throw new ApiException("User is the owner of the room");
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
                throw new ApiException("User doesn't own a room");
            }
        } else {
            long rId = roomReqId;
            if (!r.isPresent() || r.get() != rId) {
                if (true) { //TODO [ADMIN_CHECK]
                    roomId = rId;
                    if (!roomFinder.isRoomConfirmed(roomId).isPresent()) {
                        log.error("Room with id {} doesn't exist!", roomId);
                        throw new ApiException("Room not found");
                    }
                } else {
                    log.error("User is not an admin! It cannot operate on room {}", rId);
                    throw new ApiException("User is not an admin!");
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
            throw new ApiException("Room is already confirmed");
        }
    }
    public void assertRoomConfirmed(long roomId) {
        var r = roomFinder.isRoomConfirmed(roomId);
        assertRoomFound(r, roomId);
        if (!r.get()) {
            log.error("Room {} is NOT confirmed!", roomId);
            throw new ApiException("Room is NOT confirmed");
        }
    }

    public void assertRoomCanBeConfirmed(long roomId, @NotNull Event event) {
        if (!roomLogic.canConfirmRoom(roomId, event)) {
            log.error("Room {} cannot be confirmed!", roomId);
            throw new ApiException("Room cannot be confirmed");
        }
    }
    public void assertRoomCanBeUnconfirmed(long roomId) {
        if (!roomLogic.canUnconfirmRoom(roomId)) {
            log.error("Room {} cannot be unconfirmed!", roomId);
            throw new ApiException("Room cannot be unconfirmed");
        }
    }

    @NotNull public RoomGuest getRoomGuestObjFromUserEventAndAssertItExistsAndConfirmed(
            long userId, @NotNull Event event) {
        var r = roomFinder.getConfirmedRoomGuestFromUserEvent(userId, event);
        if (!r.isPresent()) {
            log.error("Could not find any roomGuest obj for user {} at event {}", userId, event);
            throw new ApiException("Could not find any roomGuest");
        }
        return r.get();
    }
    @NotNull public RoomGuest getRoomGuestObjAndAssertItExists(long guestId) {
        var r = roomFinder.getRoomGuestFromId(guestId);
        if (!r.isPresent()) {
            log.error("Unable to find roomGuest for id {}", guestId);
            throw new ApiException("Unable to find guest");
        }
        return r.get();
    }
    public void assertGuestIsNotConfirmed(RoomGuest guest) {
        if (guest.isConfirmed()) {
            log.error("Guest {} is already confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is already confirmed!");
        }
    }
    public void assertGuestIsConfirmed(RoomGuest guest) {
        if (!guest.isConfirmed()) {
            log.error("Guest {} is NOT confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is NOT confirmed!");

        }
    }

    public void assertIsGuestObjOwnerOrAdmin(RoomGuest guest, long requesterUserId) {
        boolean isAdmin = true; //TODO [ADMIN_CHECK]
        if (guest.getUserId() != requesterUserId && !isAdmin) {
            log.error("User {} has no rights over guest obj {}", requesterUserId, guest.getGuestId());
            throw new ApiException("User has no rights over specified guest!");
        }
    }

    public void assertRoomNotFull(long roomId) {
        List<RoomGuest> roomMates = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        Optional<Short> capacity = roomFinder.getRoomCapacity(roomId);

        if (!capacity.isPresent()) {
            log.error("Room {} not found while checking capacity", roomId);
            throw new ApiException("Room not found");
        }

        if (capacity.get() >= roomMates.size()) {
            log.error("Room {} is already full!", roomId);
            throw new ApiException("Room is full");
        }
    }

    public void assertOrderIsPaid(long userId, @NotNull Event event) {
        var r = orderFinder.getOrderStatus(userId, event);
        if (!r.isPresent()) {
            log.error("Order for user {} on event {} not found!", userId, event);
            throw new ApiException("Order not found");
        }
        if (r.get() != OrderStatus.PAID) {
            log.error("Order for user {} on event {} is not paid", userId, event);
            throw new ApiException("Order is not paid");
        }
    }

    private void assertOrderFound(Optional<?> r, long userId, @NotNull Event event) {
        if (!r.isPresent()) {
            log.error("No order found for user {} on event {}", userId, event);
            throw new ApiException("Order not found");
        }
    }

    private void assertRoomFound(Optional<?> o, long roomId) {
        if (!o.isPresent()) {
            log.error("Room {} was not found!", roomId);
            throw new ApiException("Room not found");
        }
    }
}
