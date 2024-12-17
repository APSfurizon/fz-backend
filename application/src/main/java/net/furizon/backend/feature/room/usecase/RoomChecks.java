package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
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
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomLogic roomLogic;

    public void assertUserHasOrderAndItsNotDaily(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        if (!isDaily.isPresent()) {
            log.error("User is trying to manage a room, but he has no registered order!");
            throw new ApiException("User has no registered order");
        }

        if (isDaily.get()) {
            log.error("User is trying to manage a room, but has a daily ticket!");
            throw new ApiException("User has a daily ticket");
        }
    }

    public void assertUserDoesNotOwnAroom(long userId, @NotNull Event event) {
        boolean alreadyOwnsAroom = roomFinder.userOwnsAroom(userId, event.getId());
        if (alreadyOwnsAroom) {
            log.error("User is trying to manage a room, but already has one!");
            throw new ApiException("User already owns a room");
        }
    }

    public void assertUserIsNotInRoom(long userId, @NotNull Event event) {
        boolean userInRoom = roomFinder.isUserInAroom(userId, event.getId());
        if (userInRoom) {
            log.error("User is trying to manage a room, but he's already in one!");
            throw new ApiException("User already is in a room");
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

    public void assertRoomCanBeConfirmed(long roomId) {
        if (!roomLogic.canConfirm(roomId)) {
            log.error("Room {} cannot be confirmed!", roomId);
            throw new ApiException("Room cannot be confirmed");
        }
    }
    public void assertRoomCanBeUnconfirmed(long roomId) {
        if (!roomLogic.canUnconfirm(roomId)) {
            log.error("Room {} cannot be unconfirmed!", roomId);
            throw new ApiException("Room cannot be unconfirmed");
        }
    }

    @NotNull public RoomGuestResponse getRoomGuestObjAndAssertItExists(long guestId) {
        var r = roomFinder.getRoomGuestFromId(guestId);
        if (!r.isPresent()) {
            log.error("Unable to find roomGuest for id {}", guestId);
            throw new ApiException("Unable to find guest");
        }
        return r.get();
    }
    public void assertGuestIsNotConfirmed(RoomGuestResponse guest) {
        if (guest.isConfirmed()) {
            log.error("Guest {} is already confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is already confirmed!");
        }
    }
    public void assertGuestIsConfirmed(RoomGuestResponse guest) {
        if (!guest.isConfirmed()) {
            log.error("Guest {} is NOT confirmed in the room {}", guest.getGuestId(), guest.getRoomId());
            throw new ApiException("Guest is NOT confirmed!");

        }
    }

    public void assertIsGuestObjOwnerOrAdmin(RoomGuestResponse guest, long requesterUserId) {
        boolean isAdmin = true; //TODO [ADMIN_CHECK]
        if (guest.getUserId() != requesterUserId && !isAdmin) {
            log.error("User {} has no rights over guest obj {}", requesterUserId, guest.getGuestId());
            throw new ApiException("User has no rights over specified guest!");
        }
    }

    public void assertRoomNotFull(long roomId) {
        List<RoomGuestResponse> roomMates = roomFinder.getRoomGuestsFromRoomId(roomId, true);
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

    private void assertRoomFound(Optional<?> o, long roomId) {
        if (!o.isPresent()) {
            log.error("Room {} was not found!", roomId);
            throw new ApiException("Room not found");
        }
    }
}
