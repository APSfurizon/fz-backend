package net.furizon.backend.feature.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
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
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final GeneralChecks checks;
    @NotNull private final RoomConfig roomConfig;

    public @NotNull RoomGuest getRoomGuestAssertPermissionCheckTimeframe(long guestId, long requesterUserId) {
        boolean isAdmin = permissionFinder.userHasPermission(requesterUserId, Permission.CAN_MANAGE_ROOMS);
        assertInTimeframeToEditRoomsAllowAdmin(requesterUserId, guestId, isAdmin);
        RoomGuest guest = getRoomGuestObjAndAssertItExists(guestId);
        assertIsGuestObjOwnerOrAdmin(guest, requesterUserId, isAdmin);
        return guest;
    }
    public long getUserIdAssertPermissionCheckTimeframe(@Nullable Long targetUserId, @NotNull FurizonUser user) {
        boolean isAdmin = permissionFinder.userHasPermission(user.getUserId(), Permission.CAN_MANAGE_ROOMS);
        assertInTimeframeToEditRoomsAllowAdmin(user.getUserId(), targetUserId, isAdmin);
        return checks.getUserIdAndAssertPermission(targetUserId, user, null, isAdmin);
    }
    public long getRoomIdAssertPermissionCheckTimeframe(long userId, @NotNull Event event, @Nullable Long roomId) {
        boolean isAdmin = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_ROOMS);
        assertInTimeframeToEditRoomsAllowAdmin(userId, roomId, isAdmin);
        return getRoomIdAndAssertPermissionsOnRoom(userId, event, roomId, isAdmin);
    }
    public void assertInTimeframeToEditRoomsAllowAdmin(long userId, @Nullable Long id, @Nullable Boolean isAdminCached) {
        if (id != null) {
            if (isAdminCached == null) {
                isAdminCached = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_ROOMS);
            }
            if (isAdminCached) {
                return;
            }
        }
        assertInTimeframeToEditRooms();
    }
    public void assertInTimeframeToEditRooms() {
        OffsetDateTime end = roomConfig.getRoomChangesEndTime();
        if (end != null && end.isBefore(OffsetDateTime.now())) {
            log.error("Editing of rooms is disabled after the date {}", end);
            throw new ApiException("Room editing timeframe has ended", GeneralResponseCodes.EDIT_TIMEFRAME_ENDED);
        }
    }

    public void assertUserHasBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        checks.assertOrderFound(r, userId, event);
        if (!r.get()) {
            log.error("User {} doesn't have purchased a room for event {}", userId, event);
            throw new ApiException("User hasn't purchased a room", RoomErrorCodes.USER_HAS_NOT_PURCHASED_A_ROOM);
        }
    }
    public void assertUserHasNotBoughtAroom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        checks.assertOrderFound(r, userId, event);
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
            throw new ApiException("User is already invited to specified room",
                    RoomErrorCodes.USER_ALREADY_INVITED_TO_ROOM);
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
        return getRoomIdAndAssertPermissionsOnRoom(userId, event, roomReqId, null);
    }
    public long getRoomIdAndAssertPermissionsOnRoom(long userId, @NotNull Event event, @Nullable Long roomReqId,
                                                    @Nullable Boolean isAdminCached) {
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
            long idRes = roomReqId;
            if (!r.isPresent() || r.get() != idRes) {
                if (isAdminCached == null) {
                    isAdminCached = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_ROOMS);
                }
                if (isAdminCached) {
                    roomId = idRes;
                    if (!roomFinder.isRoomConfirmed(roomId).isPresent()) {
                        log.error("Room with id {} doesn't exist!", roomId);
                        throw new ApiException("Room not found", RoomErrorCodes.ROOM_NOT_FOUND);
                    }
                } else {
                    log.error("User is not an admin! It cannot operate on room {}", idRes);
                    throw new ApiException("User is not an admin!", GeneralResponseCodes.USER_IS_NOT_ADMIN);
                }
            } else {
                roomId = idRes;
            }
        }

        return roomId;
    }
    public void assertPermissionsOnRoom(long userId, @NotNull Event event, long roomId,
                                        @Nullable Boolean isAdminCached) {
        getRoomIdAndAssertPermissionsOnRoom(userId, event, roomId, isAdminCached);
    }

    public void assertRoomFromCurrentEvent(long roomId, @NotNull Event event) {
        var r = roomFinder.getEventIdOfRoom(roomId);
        assertRoomFound(r, roomId);
        long eventId = r.get();
        long currentEventId = event.getId();
        if (eventId != currentEventId) {
            log.error("User is trying to manage room {}, but it's not from current event! (e: {}, ce: {})",
                    roomId, eventId, currentEventId);
            throw new ApiException("Room is not from current event", RoomErrorCodes.ROOM_NOT_OF_CURRENT_EVENT);
        }
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

    @NotNull public RoomGuest getRoomGuestObjFromUserEventAndAssertItExistsAndConfirmed(
            long userId, @NotNull Event event) {
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
        assertIsGuestObjOwnerOrAdmin(guest, requesterUserId, null);
    }
    public void assertIsGuestObjOwnerOrAdmin(RoomGuest guest, long requesterUserId, @Nullable Boolean isAdminCached) {
        if (isAdminCached == null) {
            isAdminCached = permissionFinder.userHasPermission(requesterUserId, Permission.CAN_MANAGE_ROOMS);
        }
        if (guest.getUserId() != requesterUserId && !isAdminCached) {
            log.error("User {} has no rights over guest obj {}", requesterUserId, guest.getGuestId());
            throw new ApiException("User has no rights over specified guest!", GeneralResponseCodes.USER_IS_NOT_ADMIN);
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

    public void assertExchangeExist(@Nullable ExchangeConfirmationStatus status, long exchangeId) {
        if (status == null) {
            log.error("No confirmed exchange status found for exchangeId {}", exchangeId);
            throw new ApiException("Exchange not found", RoomErrorCodes.EXCHANGE_NOT_FOUND);
        }
    }
    public void assertBothUsersHasConfirmedExchange(long exchangeId) {
        var r = exchangeConfirmationFinder.getExchangeStatusFromId(exchangeId);
        assertExchangeExist(r, exchangeId);
        assertBothUsersHasConfirmedExchange(r);
    }
    public void assertBothUsersHasConfirmedExchange(@NotNull ExchangeConfirmationStatus status) {
        if (!status.isFullyConfirmed()) {
            log.error("Exchange {} is not fully confirmed", status.getExchangeId());
            throw new ApiException("Exchange is not fully confirmed", RoomErrorCodes.EXCHANGE_NOT_FULLY_CONFIRMED);
        }
    }
    public void assertSourceUserHasNotPendingExchanges(long userId, @NotNull Event event) {
        var r = exchangeConfirmationFinder.getExchangeStatusFromSourceUsrIdEvent(userId, event);
        if (r != null) {
            log.error("User {} has still pending confirmations ({})", userId, r.getExchangeId());
            throw new ApiException("User has still pending confirmations", RoomErrorCodes.EXCHANGE_STILL_PENDING);
        }
    }
    public void assertUserHasRightsOnExchange(long userId, @NotNull ExchangeConfirmationStatus status) {
        //We don't allow admin confirmation, otherwise which side do we confirm? For the future: maybe both?
        if (status.getSourceUserId() != userId && status.getTargetUserId() != userId) {
            log.error("User {} is trying on operate on exchange {} but has no rights!", userId, status.getExchangeId());
            throw new ApiException("User has no rights on exchange", GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
    }

    private void assertRoomFound(Optional<?> o, long roomId) {
        if (!o.isPresent()) {
            log.error("Room {} was not found!", roomId);
            throw new ApiException("Room not found", RoomErrorCodes.ROOM_NOT_FOUND);
        }
    }
}
