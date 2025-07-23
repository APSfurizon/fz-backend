package net.furizon.backend.feature.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.SANITY_CHECK_REASON;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_NO_OWNER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_DAILY_TICKET;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_NOT_BOUGHT_ROOM;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_NO_ORDER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_IN_TOO_MANY_ROOMS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_NOT_IN_ROOM;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_ORDER_INVALID_STATUS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_TOO_MANY_MEMBERS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_HAS_DAILY_TICKET;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_HAS_NO_ORDER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_IN_TOO_MANY_ROOMS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_ORDER_INVALID_ORDER_STATUS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_DELETED;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_KICK_OWNER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_KICK_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomGeneralSanityCheck {
    @NotNull private final MailRoomService mailService;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomFinder roomFinder;

    private void sanityCheckLogAndStoreErrors(@Nullable List<String> logbook, String message, Object... args) {
        log.error(message, args);
        if (logbook != null) {
            logbook.add(message);
        }
    }

    private List<MailRequest> prepareSanityCheckMailRoomDeleted(long roomId,
                                                                @NotNull String roomName, @NotNull String reason) {
        return mailService.prepareBroadcastProblem(
                roomId, TEMPLATE_SANITY_CHECK_DELETED,
                MailVarPair.of(ROOM_NAME, roomName),
                MailVarPair.of(SANITY_CHECK_REASON, reason)
        );
    }
    private List<MailRequest> prepareSanityCheckMailUserKicked(long ownerId, long userId, @NotNull String fursonaName,
                                                               @NotNull String roomName, @NotNull String reason) {
        return mailService.prepareProblem(
                new MailRequest(
                        ownerId, userFinder, TEMPLATE_SANITY_CHECK_KICK_OWNER,
                        MailVarPair.of(OTHER_FURSONA_NAME, fursonaName),
                        MailVarPair.of(ROOM_NAME, roomName),
                        MailVarPair.of(SANITY_CHECK_REASON, reason)
                ),
                new MailRequest(
                        userId, userFinder, TEMPLATE_SANITY_CHECK_KICK_USER,
                        MailVarPair.of(ROOM_NAME, roomName),
                        MailVarPair.of(SANITY_CHECK_REASON, reason)
                )
        );
    }

    public void doSanityChecks(long roomId,
                               @NotNull RoomLogic roomLogic,
                               @NotNull PretixInformation pretixInformation,
                               @Nullable List<String> detectedErrors) {
        log.info("[ROOM SANITY CHECKS] Running room sanity check on room {}", roomId);
        final Event event = pretixInformation.getCurrentEvent();
        final long eventId = event.getId();
        List<MailRequest> mails = new LinkedList<>();

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
        final List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, event);
        final List<RoomGuestResponse> confirmedGuests =
                guests.stream().filter(k -> k.getRoomGuest().isConfirmed()).toList();
        var roomConfirmed = roomFinder.isRoomConfirmed(roomId);
        boolean checkAfterConfirmation = true;
        if (roomConfirmed.isEmpty()) {
            //delete room, it wasn't found
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Unable to fetch room while checking if it was confirmed", roomId);
        } else {
            checkAfterConfirmation = !roomLogic.isConfirmationSupported() || roomConfirmed.get();
        }

        String roomName = roomFinder.getRoomName(roomId);
        roomName = roomName == null ? "" : roomName;

        var ownerIdOpt = roomFinder.getOwnerUserIdFromRoomId(roomId);
        if (!ownerIdOpt.isPresent()) {
            //delete room, no owner found
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "No owner found in room {}. Deleting the room", roomId);
            mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_NO_OWNER));
            roomLogic.deleteRoom(roomId);
            return;
        }
        long ownerId = ownerIdOpt.get();
        Order order = orderFinder.findOrderByUserIdEvent(ownerId, event, pretixInformation);
        if (order == null) {
            //delete room, owner has no order
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has no order. Deleting the room", ownerId, roomId);
            mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NO_ORDER));
            roomLogic.deleteRoom(roomId);
            return;
        }
        if (checkAfterConfirmation && !order.hasRoom()) {
            //delete room, owner has no room
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} hasn't bought a room. Deleting the room", ownerId, roomId);
            mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NOT_BOUGHT_ROOM));
            roomLogic.deleteRoom(roomId);
            return;
        }
        if (order.isDaily()) {
            //delete room, owner has daily ticket
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has a daily ticket. Deleting the room", ownerId, roomId);
            mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_DAILY_TICKET));
            roomLogic.deleteRoom(roomId);
            return;
        }

        if (checkAfterConfirmation) {
            int guestNo = confirmedGuests.size();
            int capacity = (int) order.getRoomCapacity();
            if (guestNo > capacity) {
                //delete room, too many members!
                sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Room {} has too many members ({} > {}). Deleting the room", roomId, guestNo, capacity);
                mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_TOO_MANY_MEMBERS));
                roomLogic.deleteRoom(roomId);
                return;
            }
        }

        boolean ownerFound = false;
        for (RoomGuestResponse guest : confirmedGuests) {
            long usrId = guest.getUser().getUserId();
            long guestId = guest.getRoomGuest().getGuestId();

            UserEmailData mail = userFinder.getMailDataForUser(usrId);
            String fursona = mail == null ? null : mail.getFursonaName();
            fursona = fursona == null ? "" : fursona;

            //This (together with owner in room check) will also test if a owner has multiple rooms
            int roomNo = roomFinder.countRoomsWithUser(usrId, eventId);
            if (roomNo > 1) {
                if (usrId == ownerId) {
                    //delete room, owner is in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "Owner {} of room {} is in too many rooms ({})!. Deleting the room",
                            usrId, roomId, roomNo);
                    mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_IN_TOO_MANY_ROOMS));
                    roomLogic.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he's in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                                    + "User {}g{} of room {} is in too many rooms ({})!. Kicking the user",
                            usrId, guestId, roomId, roomNo);
                    mails.addAll(prepareSanityCheckMailUserKicked(
                            ownerId, usrId, fursona, roomName, SC_USER_IN_TOO_MANY_ROOMS
                    ));
                    roomLogic.kickFromRoom(guestId);
                    continue;
                }
            }

            OrderStatus status = guest.getOrderStatus();
            if (status == null || status == OrderStatus.CANCELED) {
                if (usrId == ownerId) {
                    //delete room, owner's order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "Owner {} of room {} has a canceled order!. Deleting the room", usrId, roomId);
                    mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_ORDER_INVALID_STATUS));
                    roomLogic.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, his order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "User {}g{} of room {} has a canceled order!. Kicking the user", usrId, guestId, roomId);
                    mails.addAll(prepareSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName,
                            SC_USER_ORDER_INVALID_ORDER_STATUS));
                    roomLogic.kickFromRoom(guestId);
                    continue;
                }
            }

            var daily = orderFinder.isOrderDaily(usrId, event);
            if (!daily.isPresent()) {
                if (usrId == ownerId) {
                    //delete room, owner has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "Owner {} of room {} has no order. Deleting the room", usrId, roomId);
                    mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NO_ORDER));
                    roomLogic.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, user has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "User {}g{} of room {} has no order. Kicking the user", usrId, guestId, roomId);
                    mails.addAll(
                            prepareSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName, SC_USER_HAS_NO_ORDER)
                    );
                    roomLogic.kickFromRoom(guestId);
                    continue;
                }
            }
            if (daily.get()) {
                if (usrId == ownerId) {
                    //delete room, owner has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "Owner {} of room {} has a daily ticket. Deleting the room", usrId, roomId);
                    mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_DAILY_TICKET));
                    roomLogic.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                            + "User {}g{} of room {} has a daily ticket. Kicking the user", usrId, guestId, roomId);
                    mails.addAll(prepareSanityCheckMailUserKicked(
                            ownerId, usrId, fursona, roomName, SC_USER_HAS_DAILY_TICKET
                    ));
                    roomLogic.kickFromRoom(guestId);
                    continue;
                }
            }

            if (usrId == ownerId) {
                ownerFound = true;
            }
        }

        if (!ownerFound) {
            //delete room, owner is not in room!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} was not found in the room's guest. Deleting the room", ownerId, roomId);
            mails.addAll(prepareSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_NOT_IN_ROOM));
            roomLogic.deleteRoom(roomId);
            return;
        }

        mailService.fireAndForgetMany(mails);
    }
}
