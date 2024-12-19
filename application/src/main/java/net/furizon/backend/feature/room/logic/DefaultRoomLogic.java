package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiFunction;

import static net.furizon.jooq.generated.tables.Rooms.ROOMS;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.RoomGuests.ROOM_GUESTS;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-default", matchIfMissing = true)
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
    public long createRoom(String name, long userId, @NotNull Event event) {
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
            throw new ApiException("Order not found while creating a room", RoomErrorCodes.ORDER_NOT_FOUND);
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
        this.invitePersonToRoom(userId, roomId, event, true, true);
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
    @Transactional
    public long invitePersonToRoom(
            long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit
    ) {
        log.info("Inviting user {} to room {} on event {}. Force = {}; forceExit = {}",
                invitedUserId, roomId, event, force, forceExit);

        boolean shouldDeleteEntries = false;
        boolean alreadyInAroom = roomFinder.isUserInAroom(invitedUserId, event.getId());
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
                condition = condition.and(ROOM_GUESTS.CONFIRMED.eq(true));
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
    public boolean inviteAccept(long guestId, long invitedUserId, @NotNull Event event) {
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
    public boolean canConfirmRoom(long roomId, @NotNull Event event) {
        boolean everyonePaid = true;
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        for (RoomGuest guest : guests) {
            var r = orderFinder.getOrderStatus(guest.getUserId(), event);
            everyonePaid &= r.isPresent() && r.get() == OrderStatus.PAID;
        }
        return everyonePaid;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        log.info("Room {} has been confirmed!", roomId);
        //Deletes pending invitations
        command.execute(
            PostgresDSL.deleteFrom(ROOM_GUESTS)
            .where(
                ROOM_GUESTS.ROOM_ID.eq(roomId)
                .and(ROOM_GUESTS.CONFIRMED.eq(false))
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
        return false;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId) {
        log.warn("DefaultRoomLogic does not implement canUnconfirmRoom! Default `true` value is returned");
        return true;
    }

    @Override
    public boolean unconfirmRoom(long roomId) {
        log.info("Room {} has been unconfirmed!", roomId);
        return command.execute(
            PostgresDSL.update(ROOMS)
                .set(ROOMS.ROOM_CONFIRMED, false)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
    }

    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return exchange(targetUsrId, sourceUsrId, roomId, event, pretixInformation, (targetGuest, sourceGuest) ->
            command.execute(
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
                .where(
                    ROOMS.ROOM_ID.eq(roomId)
                )
            ) > 0
        );
    }

    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return exchange(targetUsrId, sourceUsrId, roomId, event, pretixInformation, (targetGuest, sourceGuest) ->
            command.execute(
                PostgresDSL.update(ORDERS)
                .set(ORDERS.USER_ID, targetUsrId)
                .where(
                    ORDERS.USER_ID.eq(sourceUsrId)
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                )
            ) > 0
        );
    }

    private boolean exchange(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation, BiFunction<RoomGuest, RoomGuest, Boolean> updateDbFnc) {
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, false);
        RoomGuest targetGuest = guests.stream().filter(g -> g.getUserId() == targetUsrId).findFirst().orElse(null);
        RoomGuest sourceGuest = guests.stream().filter(g -> g.getUserId() == sourceUsrId).findFirst().orElse(null);
        if (sourceGuest == null) {
            log.error("No guest found in room {} with userId {}", roomId, sourceUsrId);
            throw new ApiException("No guest found", RoomErrorCodes.GUEST_NOT_FOUND);
        }

        boolean result = true;

        if (targetGuest == null) {
            //Target is not in the room
            result &= this.kickFromRoom(sourceGuest.getGuestId());
            result &= this.invitePersonToRoom(targetUsrId, roomId, event, true, true) > 0L;

        } else if (targetGuest != null && targetGuest.isConfirmed()) {
            //Target is inside the room and accepted the invitation
            result &= true;

        } else if (targetGuest != null && !targetGuest.isConfirmed()) {
            //Target was invited to the room, but hasn't accepted yet
            result &= this.kickFromRoom(sourceGuest.getGuestId());
            result &= this.inviteAccept(targetGuest.getGuestId(), targetUsrId, event);

        } else {
            log.error("No proper room exchange case. We shouldn't be here... {}", targetUsrId);
            result &= false;
        }

        if (result) {
            result &= updateDbFnc.apply(targetGuest, sourceGuest);
            if (!result) {
                log.error("Unable to update db in an exchange. srcUsr {}; dstUsr {}; srcGuest {}; dstGuest {}; event {}",
                    sourceUsrId, targetUsrId, sourceGuest.getGuestId(), targetGuest == null ? -1L : targetGuest.getGuestId(), event);
            }
        } else {
            log.error("Unable to modify room members in a exchange. srcUsr {}; dstUsr {}; srcGuest {}; dstGuest {}; event {}",
                    sourceUsrId, targetUsrId, sourceGuest.getGuestId(), targetGuest == null ? -1L : targetGuest.getGuestId(), event);
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

    private boolean refund(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        var r = roomFinder.getRoomIdFromOwnerUserId(userId, event);
        if (!r.isPresent()) {
            log.error("No room found for user {} in event {} while refunding", userId, event);
            throw new ApiException("No room find in exchange", RoomErrorCodes.ROOM_NOT_FOUND);
        }
        return this.deleteRoom(r.get());
    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation, @Nullable List<String> detectedErrors) {
        log.warn("DefaultRoomLogic does not implement any sanity check!");
    }
}
