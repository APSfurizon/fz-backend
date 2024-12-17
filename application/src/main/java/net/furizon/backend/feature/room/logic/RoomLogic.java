package net.furizon.backend.feature.room.logic;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface RoomLogic {
    //Security checks must be done BEFORE calling the following methods!

    long createRoom(String name, long userId, @NotNull Event event);
    boolean deleteRoom(long roomId);
    boolean changeRoomName(String name, long roomId);

    //force = bypass invitation logic, forceExit = if the user is in another room, add him to the current one forcefully
    long invitePersonToRoom(long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit);
    boolean inviteAccept(long guestId, long roomId);
    boolean inviteRefuse(long guestId);
    boolean inviteCancel(long guestId);

    boolean kickFromRoom(long guestId);
    boolean leaveRoom(long guestId);

    boolean canConfirm(long roomId);
    boolean confirmRoom(long roomId);
    boolean canUnconfirm(long roomId);
    boolean unconfirmRoom(long roomId);

    void doSanityChecks();
}
