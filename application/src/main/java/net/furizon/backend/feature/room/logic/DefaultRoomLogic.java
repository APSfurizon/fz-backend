package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional
    public long createRoom(String name, long userId, @NotNull Event event) {
        log.info("Creating a room for user {} on event {} with name '{}'", userId, event, name);
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
        }
        return command.executeResult(
            PostgresDSL.insertInto(
                ROOMS,
                ROOMS.ROOM_NAME,
                ROOMS.ORDER_ID
            ).values(
                name,
                r.get().get(ORDERS.ID)
            ).returning(
                ROOMS.ROOM_ID
            )
        ).getFirst().get(ROOMS.ROOM_ID);
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

        boolean alreadyInAroom = roomFinder.isUserInAroom(invitedUserId, event.getId());
        if (alreadyInAroom) {
            if (force && !forceExit) {
                log.error("User {} is already in a room in event {}, but forceExit was equal to false",
                        invitedUserId, event);
                throw new ApiException("User is already in a room");
            }
            if (forceExit) {
                command.execute(
                    PostgresDSL.deleteFrom(ROOM_GUESTS)
                    .where(
                        ROOM_GUESTS.USER_ID.eq(invitedUserId)
                        .and(ROOM_GUESTS.CONFIRMED.eq(true))
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
            }
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
    public boolean inviteAccept(long guestId, long roomId) {
        log.info("Guest {} has accepted invitation to room {}", guestId, roomId);
        return command.execute(
            PostgresDSL.update(ROOM_GUESTS)
            .set(ROOM_GUESTS.CONFIRMED, true)
            .where(
                ROOM_GUESTS.ROOM_GUEST_ID.eq(guestId)
                .and(ROOM_GUESTS.ROOM_ID.eq(roomId))
            )
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
    public boolean canConfirm(long roomId, @NotNull Event event) {
        boolean everyonePaid = true;
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        for (RoomGuest guest : guests) {
            var r = orderFinder.getOrderStatus(guest.getUserId(), event);
            everyonePaid &= r.isPresent() && r.get() == OrderStatus.PAID;
        }
        return everyonePaid;
    }

    @Override
    public boolean canUnconfirm(long roomId) {
        log.warn("DefaultRoomLogic does not implement canUnconfirm! Default `true` value is returned");
        return true;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        log.info("Room {} has been confirmed!", roomId);
        return command.execute(
            PostgresDSL.update(ROOMS)
            .set(ROOMS.ROOM_CONFIRMED, true)
            .where(ROOMS.ROOM_ID.eq(roomId))
        ) > 0;
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
    public void doSanityChecks() {
        log.warn("DefaultRoomLogic does not implement any sanity check!");
    }
}
