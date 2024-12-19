package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.usecase.RoomChecks;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-full-room")
public class UserBuysFullRoom implements RoomLogic {
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        return r.isPresent() && r.get() && defaultRoomLogic.canCreateRoom(userId, event);
    }

    @Override
    public long createRoom(String name, long userId, @NotNull Event event) {
        checks.assertUserHasBoughtAroom(userId, event);
        return defaultRoomLogic.createRoom(name, userId, event);
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
    public long invitePersonToRoom(
            long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit) {
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.invitePersonToRoom(invitedUserId, roomId, event, force, forceExit);
    }

    @Override
    public boolean inviteAccept(long guestId, long invitedUserId, @NotNull Event event) {
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.inviteAccept(guestId, invitedUserId, event);
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
        return false;
    }

    @Override
    public boolean canConfirmRoom(long roomId, @NotNull Event event) {
        return false;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        return defaultRoomLogic.confirmRoom(roomId);
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return false;
    }

    @Override
    public boolean unconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        /*
        - Create new room position on target user
        - Remove room position from source user
        - Create refund in DONE state for source user
        - Create payment in CONFIRMED state for target user
         */
        return defaultRoomLogic.exchangeRoom(targetUsrId, sourceUsrId, roomId, event, pretixInformation);
    }

    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        // - Update userId answer
        return defaultRoomLogic.exchangeFullOrder(targetUsrId, sourceUsrId, roomId, event, pretixInformation);
    }

    @Override
    public boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false; //TODO
    }

    @Override
    public boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false; //TODO
    }

    private void sanityCheckLogAndStoreErrors(@Nullable List<String> logbook, String message, Object... args) {
        log.error(message, args);
        if (logbook != null) {
            logbook.add(message);
        }
    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation, @Nullable List<String> detectedErrors) {
        log.info("[ROOM SANITY CHECKS] Running room sanity check on room {}", roomId);
        Event event = pretixInformation.getCurrentEvent();
        long eventId = event.getId();
        //The following checks are done:
        //- User in multiple rooms
        //- Members exceed room maximum
        //- Owner not in room
        //- Owner has multiple rooms
        //- Owner order status == canceled
        //- Owner doesn't own a room
        //- Owner has a daily ticket
        //- User order status == canceled
        //- User order is daily

        //RoomInfo info = roomFinder.getRoomInfoForUser(userId, input.event, input.pretixInformation);
        List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, event);
        List<RoomGuestResponse> confirmedGuests = guests.stream().filter(k -> k.getRoomGuest().isConfirmed()).toList();


        var ownerIdOpt = roomFinder.getOwnerUserIdFromRoomId(roomId);
        if (!ownerIdOpt.isPresent()) {
            //delete room, no owner found
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] No owner found in room {}. Deleting the room", roomId);
            this.deleteRoom(roomId);
            return;
        }
        long ownerId = ownerIdOpt.get();
        Order order = orderFinder.findOrderByUserIdEvent(ownerId, event, pretixInformation);
        if (order == null) {
            //delete room, owner has no order
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} has no order. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
        if (!order.hasRoom()) {
            //delete room, owner has no room
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} hasn't bought a room. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
        if (order.isDaily()) {
            //delete room, owner has daily ticket
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} has a daily ticket. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }

        int guestNo = confirmedGuests.size();
        int capacity = (int) order.getRoomCapacity();
        if (guestNo > capacity) {
            //delete room, too many members!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Room {} has too many members ({} > {}). Deleting the room", roomId, guestNo, capacity);
            this.deleteRoom(roomId);
            return;
        }

        boolean ownerFound = false;
        for (RoomGuestResponse guest : confirmedGuests) {
            long usrId = guest.getUser().getUserId();
            long guestId = guest.getRoomGuest().getGuestId();

            //This (together with owner in room check) will also test if a owner has multiple rooms
            int roomNo = roomFinder.countRoomsWithUser(usrId, eventId);
            if (roomNo > 1) {
                if (usrId == ownerId) {
                    //delete room, owner is in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} is in too many rooms ({})!. Deleting the room", usrId, roomId, roomNo);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he's in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] User {}g{} of room {} is in too many rooms ({})!. Kicking the user", usrId, guestId, roomId, roomNo);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            OrderStatus status = guest.getOrderStatus();
            if (status == null || status == OrderStatus.CANCELED) {
                if (usrId == ownerId) {
                    //delete room, owner's order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} has a canceled order!. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, his order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] User {}g{} of room {} has a canceled order!. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            var daily = orderFinder.isOrderDaily(usrId, event);
            if (!daily.isPresent()) {
                if (usrId == ownerId) {
                    //delete room, owner has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} has no order. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, user has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] User {}g{} of room {} has no order. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }
            if (daily.get()) {
                if (usrId == ownerId) {
                    //delete room, owner has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} has a daily ticket. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] User {}g{} of room {} has a daily ticket. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            if (usrId == ownerId) {
                ownerFound = true;
            }
        }

        if (!ownerFound) {
            //delete room, owner is not in room!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] Owner {} of room {} was not found in the room's guest. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
    }
}
