package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiFunction;

import static net.furizon.jooq.generated.Tables.ROOMS;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultRoomLogic implements RoomLogic {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final SqlCommand command;
    @NotNull private final SqlQuery query;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        return !roomFinder.userOwnsAroom(userId, event.getId());
    }

    @Override
    @Transactional
    public long createRoom(String name, long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("Creating a room for user {} on event {} with name '{}'", userId, event, name);
        //Get order id
        var r = query.fetchFirst(
            PostgresDSL.select(ORDERS.ID)
                .from(ORDERS)
                .where(
                    ORDERS.USER_ID.eq(userId)
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                )
        );
        if (!r.isPresent()) {
            log.error("Order not found while creating a room for user {} and event {}", userId, event);
            throw new ApiException("Order not found while creating a room", GeneralResponseCodes.ORDER_NOT_FOUND);
        }
        long orderId = r.get().get(ORDERS.ID);
        //Actual creation of the room
        long roomId = command.executeResult(
            PostgresDSL.insertInto(
                ROOMS,
                ROOMS.ROOM_NAME,
                ROOMS.ORDER_ID
            ).values(
                name,
                orderId
            ).returning(
                ROOMS.ROOM_ID
            )
        ).getFirst().get(ROOMS.ROOM_ID);
        //Insertion of the owner in the room
        this.invitePersonToRoom(userId, roomId, event, pretixInformation, true, true);
        return roomId;
    }

    @Override
    public boolean deleteRoom(long roomId) {
        log.info("Deleting room {}", roomId);
        return command.execute(
                PostgresDSL.deleteFrom(ROOMS)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    public boolean changeRoomName(String name, long roomId) {
        log.info("Changing name of room {} to '{}'", roomId, name);
        return command.execute(
                PostgresDSL.update(ROOMS)
                .set(ROOMS.ROOM_NAME, name)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    public boolean setShowInNosecount(boolean showInNosecount, long roomId) {
        log.info("Setting showInNosecount={} for room {}", showInNosecount, roomId);
        return command.execute(
                PostgresDSL.update(ROOMS)
                .set(ROOMS.SHOW_IN_NOSECOUNT, showInNosecount)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    @Transactional
    public long invitePersonToRoom(
            long invitedUserId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation, boolean force, boolean forceExit
    ) {
        log.info("Inviting user {} to room {} on event {}. Force = {}; forceExit = {}",
                invitedUserId, roomId, event, force, forceExit);



        boolean shouldDeleteEntries = false;
        boolean alreadyInAroom = roomFinder.isUserInAroom(invitedUserId, event.getId(), false);
        if (alreadyInAroom) {
            if (forceExit) {
                shouldDeleteEntries = true;
            } else {
                log.error("User {} is already in a room in event {}, but forceExit was equal to false",
                        invitedUserId, event);
                throw new ApiException("User is already in a room", RoomErrorCodes.USER_ALREADY_IS_IN_A_ROOM);
            }
        }

        if (shouldDeleteEntries || force) {
            Condition condition =
                    ROOM_GUESTS.USER_ID.eq(invitedUserId)
                    .and(ROOM_GUESTS.ROOM_ID.in(
                        PostgresDSL.select(ROOMS.ROOM_ID)
                        .from(ROOMS)
                        .join(ORDERS)
                        .on(
                            ROOMS.ORDER_ID.eq(ORDERS.ID)
                            .and(ORDERS.EVENT_ID.eq(event.getId()))
                        )
                    ));
            if (!force) {
                condition = condition.and(ROOM_GUESTS.CONFIRMED.isTrue());
            }
            //Delete previous confirmed invitations
            command.execute(
                PostgresDSL.deleteFrom(ROOM_GUESTS)
                .where(condition)
            );
        }

        return command.executeResult(
            PostgresDSL.insertInto(
                ROOM_GUESTS,
                ROOM_GUESTS.ROOM_ID,
                ROOM_GUESTS.USER_ID,
                ROOM_GUESTS.CONFIRMED
            ).values(
                roomId,
                invitedUserId,
                force
            ).returning(ROOM_GUESTS.ROOM_GUEST_ID)
        ).getFirst().get(ROOM_GUESTS.ROOM_GUEST_ID);
    }

    @Override
    @Transactional
    public boolean inviteAccept(long guestId, long invitedUserId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("Guest {} has accepted room invitation", guestId);
        //Deletes pending invitation for user in event
        command.execute(
            PostgresDSL.deleteFrom(ROOM_GUESTS)
            .where(
                ROOM_GUESTS.USER_ID.eq(invitedUserId)
                .and(ROOM_GUESTS.ROOM_GUEST_ID.notEqual(guestId))
                .and(ROOM_GUESTS.ROOM_ID.in(
                    PostgresDSL.select(ROOMS.ROOM_ID)
                    .from(ROOMS)
                    .join(ORDERS)
                    .on(
                        ROOMS.ORDER_ID.eq(ORDERS.ID)
                        .and(ORDERS.EVENT_ID.eq(event.getId()))
                    )
                ))
            )
        );

        return command.execute(
            PostgresDSL.update(ROOM_GUESTS)
            .set(ROOM_GUESTS.CONFIRMED, true)
            .where(ROOM_GUESTS.ROOM_GUEST_ID.eq(guestId))
        ) > 0;
    }

    private boolean deleteGuest(long guestId) {
        return command.execute(
            PostgresDSL.deleteFrom(ROOM_GUESTS)
            .where(ROOM_GUESTS.ROOM_GUEST_ID.eq(guestId))
        ) > 0;
    }

    @Override
    public boolean inviteRefuse(long guestId) {
        log.info("Guest {} has refused the room invitation", guestId);
        return deleteGuest(guestId);
    }

    @Override
    public boolean inviteCancel(long guestId) {
        log.info("Room owner has canceled guest invitation {}", guestId);
        return deleteGuest(guestId);
    }

    @Override
    public boolean kickFromRoom(long guestId) {
        log.info("Guest {} has been kicked from the room", guestId);
        return deleteGuest(guestId);
    }

    @Override
    public boolean leaveRoom(long guestId) {
        log.info("Guest {} has left his room", guestId);
        return deleteGuest(guestId);
    }

    @Override
    public boolean isConfirmationSupported() {
        return true;
    }

    @Override
    public boolean canConfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        if (!isConfirmationSupported()) {
            return false;
        }

        var confirmed = roomFinder.isRoomConfirmed(roomId);
        if (confirmed.isEmpty() || confirmed.get()) {
            return false;
        }

        boolean everyonePaid = true;
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        for (RoomGuest guest : guests) {
            var r = orderFinder.getOrderStatus(guest.getUserId(), event);
            everyonePaid &= r.isPresent() && r.get() == OrderStatus.PAID;
        }
        return everyonePaid;
    }

    @Override
    public boolean confirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("Room {} has been confirmed!", roomId);
        //Deletes pending invitations
        command.execute(
            PostgresDSL.deleteFrom(ROOM_GUESTS)
            .where(
                ROOM_GUESTS.ROOM_ID.eq(roomId)
                .and(ROOM_GUESTS.CONFIRMED.isFalse())
            )
        );

        return command.execute(
            PostgresDSL.update(ROOMS)
                .set(ROOMS.ROOM_CONFIRMED, true)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return true;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        if (!isUnconfirmationSupported()) {
            return false;
        }
        var confirmed = roomFinder.isRoomConfirmed(roomId);
        return confirmed.orElse(false);
    }

    @Override
    public boolean unconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("Room {} has been unconfirmed!", roomId);
        return command.execute(
            PostgresDSL.update(ROOMS)
                .set(ROOMS.ROOM_CONFIRMED, false)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    public boolean isExchangeRoomSupported(@NotNull Event event) {
        return true;
    }

    @Override
    public boolean isExchangeFullOrderSupported(@NotNull Event event) {
        return true;
    }

    public void logExchangeError(boolean result, int step, long destUserId, long sourceUserId, @NotNull Event event) {
        if (!result) {
            log.error("[ROOM_EXCHANGE] result was false after executing step {} "
                    + "while exchanging between users {} -> {} on event {}", step, sourceUserId, destUserId, event);
        }
    }

    @Transactional
    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId,
                                @Nullable Long targetRoomId, @Nullable Long sourceRoomId,
                                @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.debug("exchangeRoom() called with: "
                + "targetUsrId={}; sourceUsrId={}; targetRoomId={}; sourceRoomId={}; event = {}",
                targetUsrId, sourceUsrId, targetRoomId, sourceRoomId, event);

        return exchange(targetUsrId, sourceUsrId, sourceRoomId, targetRoomId, event, pretixInformation,
            (targetGuest, sourceGuest) -> {
                boolean result = true;

                //Check if a full room swap is needed
                if (targetRoomId != null && sourceRoomId != null && !sourceRoomId.equals(targetRoomId)) {

                    //Swap the rooms between the two people
                    //if only one of them had a room, it would have been already handled by the switchRooms function
                    result = result && command.execute(
                        PostgresDSL
                        .update(ROOM_GUESTS)
                        .set(ROOM_GUESTS.ROOM_ID, targetRoomId)
                        .where(ROOM_GUESTS.ROOM_GUEST_ID.eq(sourceGuest.getGuestId()))
                    ) > 0;
                    logExchangeError(result, 10, targetUsrId, sourceUsrId, event);
                    result = result && command.execute(
                        PostgresDSL
                        .update(ROOM_GUESTS)
                        .set(ROOM_GUESTS.ROOM_ID, sourceRoomId)
                        .where(ROOM_GUESTS.ROOM_GUEST_ID.eq(targetGuest.getGuestId()))
                    ) > 0;
                    logExchangeError(result, 11, targetUsrId, sourceUsrId, event);
                }

                if (targetRoomId != null) {
                    result = result && command.execute(
                        PostgresDSL.update(ROOMS)
                            .set(
                                ROOMS.ORDER_ID,
                                PostgresDSL.select(ORDERS.ID)
                                .from(ORDERS)
                                .where(
                                    ORDERS.USER_ID.eq(sourceUsrId)
                                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                                )
                                .limit(1)
                            )
                            .where(ROOMS.ROOM_ID.eq(targetRoomId))
                    ) > 0;
                    logExchangeError(result, 12, targetUsrId, sourceUsrId, event);
                }

                if (sourceRoomId != null) {
                    result = result && command.execute(
                        PostgresDSL.update(ROOMS)
                        .set(
                            ROOMS.ORDER_ID,
                            PostgresDSL.select(ORDERS.ID)
                                .from(ORDERS)
                                .where(
                                    ORDERS.USER_ID.eq(targetUsrId)
                                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                                )
                                .limit(1)
                        )
                        .where(ROOMS.ROOM_ID.eq(sourceRoomId))
                    ) > 0;
                    logExchangeError(result, 13, targetUsrId, sourceUsrId, event);
                }
                return result;
            }
        );
    }

    @Transactional
    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId,
                                     @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.debug("exchangeFullOrder() called with: "
                + "targetUsrId={}; sourceUsrId={}; roomId={}; event = {}",
                targetUsrId, sourceUsrId, roomId, event);
        return exchange(targetUsrId, sourceUsrId, roomId, null, event, pretixInformation,
            (targetGuest, sourceGuest) -> {
                boolean result = command.execute(
                    PostgresDSL.update(ORDERS)
                    .set(ORDERS.USER_ID, targetUsrId)
                    .where(
                        ORDERS.USER_ID.eq(sourceUsrId)
                        .and(ORDERS.EVENT_ID.eq(event.getId()))
                    )
                ) > 0;
                logExchangeError(result, 0, targetUsrId, sourceUsrId, event);
                return result;
            }
        );
    }

    @Override
    public boolean isRefundRoomSupported(@NotNull Event event) {
        return true;
    }

    @Override
    public boolean isRefundFullOrderSupported(@NotNull Event event) {
        return true;
    }

    private RoomGuest switchRooms(long targetUsrId, long sourceUsrId, long roomId,
                                         boolean targetHasRoom, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.debug("switchRooms() called with: "
                + "targetUsrId={}; sourceUsrId={}; roomId={}; targetHasRoom={}; event = {}",
                targetUsrId, sourceUsrId, roomId, targetHasRoom, event);
        RoomGuest targetGuest = null;
        RoomGuest sourceGuest = null;

        List<RoomGuest> srcGuests = roomFinder.getRoomGuestsFromRoomId(roomId, false);
        sourceGuest = srcGuests.stream().filter(g -> g.getUserId() == sourceUsrId).findFirst().orElse(null);
        if (sourceGuest == null) {
            log.error("[ROOM_EXCHANGE] No guest found in room {} with userId {}", roomId, sourceUsrId);
            throw new ApiException("No guest found", RoomErrorCodes.GUEST_NOT_FOUND);
        }

        boolean res;
        if (!targetHasRoom) {
            targetGuest = srcGuests.stream().filter(g -> g.getUserId() == targetUsrId).findFirst().orElse(null);

            if (targetGuest == null) {
                //Target is not in the room
                res = this.kickFromRoom(sourceGuest.getGuestId());
                logExchangeError(res, 0, targetUsrId, sourceUsrId, event);
                res = this.invitePersonToRoom(targetUsrId, roomId, event, pretixInformation, true, true) > 0L;
                logExchangeError(res, 1, targetUsrId, sourceUsrId, event);

            } else if (targetGuest != null && targetGuest.isConfirmed()) {
                //Target is inside the room and accepted the invitation

            } else if (targetGuest != null && !targetGuest.isConfirmed()) {
                //Target was invited to the room, but hasn't accepted yet
                res = this.kickFromRoom(sourceGuest.getGuestId());
                logExchangeError(res, 2, targetUsrId, sourceUsrId, event);
                res = this.inviteAccept(targetGuest.getGuestId(), targetUsrId, roomId, event, pretixInformation);
                logExchangeError(res, 3, targetUsrId, sourceUsrId, event);

            } else {
                log.error("[ROOM_EXCHANGE] No proper room exchange case. We shouldn't be here... {}", targetUsrId);
            }
        }

        return sourceGuest;
    }
    private boolean exchange(long targetUsrId, long sourceUsrId,
                             @Nullable Long sourceRoom, @Nullable Long targetRoom,
                             @NotNull Event event, @NotNull PretixInformation pretixInformation, BiFunction<RoomGuest, RoomGuest, Boolean> updateDbFnc) {
        log.info("[ROOM_EXCHANGE] DefaultRoomLogic: Exchange between users: {} -> {} on event {}",
                sourceUsrId, targetUsrId, event);
        boolean result = true;
        RoomGuest targetGuest = null;
        RoomGuest sourceGuest = null;

        boolean sourceHasRoom = sourceRoom != null;
        boolean targetHasRoom = targetRoom != null;

        if (sourceHasRoom) {
            sourceGuest = switchRooms(targetUsrId, sourceUsrId, sourceRoom, targetHasRoom, event, pretixInformation);
        }
        if (targetHasRoom) {
            targetGuest = switchRooms(sourceUsrId, targetUsrId, targetRoom, sourceHasRoom, event, pretixInformation);
        }

        if (targetGuest != null || sourceGuest != null) {
            result = updateDbFnc.apply(targetGuest, sourceGuest);
            if (!result) {
                log.error("[ROOM_EXCHANGE] Unable to update db in an exchange. "
                        + "srcUsr {}; dstUsr {}; srcGuest {}; dstGuest {}; event {}",
                        sourceUsrId, targetUsrId, sourceGuest,
                        targetGuest == null ? -1L : targetGuest.getGuestId(), event);
            }
        } else {
            log.error("[ROOM_EXCHANGE] Unable to modify room members in a exchange. "
                    + "srcUsr {}; dstUsr {}; srcGuest {}; dstGuest {}; event {}",
                    sourceUsrId, targetUsrId, sourceGuest,
                    targetGuest == null ? -1L : targetGuest.getGuestId(), event);
        }

        return result;
    }

    @Override
    public boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return refund(userId, event, pretixInformation);
    }

    @Override
    public boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return refund(userId, event, pretixInformation);
    }

    @Override
    public boolean isRoomBuyOrUpgradeSupported(@NotNull Event even) {
        return false;
    }

    @Override
    public boolean buyOrUpgradeRoom(
            long newRoomItemId, long newRoomPrice, @Nullable Long oldRoomPaid, long userId, @Nullable Long roomId,
            @Nullable Long newEarlyItemId, @Nullable Long newEarlyPrice, @Nullable Long oldEarlyPaid,
            @Nullable Long newLateItemId, @Nullable Long newLatePrice, @Nullable Long oldLatePaid,
            @NotNull Order order, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("DefaultRoomLogic does not implement buying or upgrading room!");
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
    }

    private boolean refund(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
        if (!r.isPresent()) {
            log.error("No room found for user {} in event {} while refunding", userId, event);
            throw new ApiException("No room find in exchange", RoomErrorCodes.ROOM_NOT_FOUND);
        }
        return this.deleteRoom(r.get());
    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation,
                               @Nullable List<String> detectedErrors) {
        log.warn("DefaultRoomLogic does not implement any sanity check!");
    }
}
