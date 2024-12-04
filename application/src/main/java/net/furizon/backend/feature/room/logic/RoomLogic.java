package net.furizon.backend.feature.room.logic;

public interface RoomLogic {
    //Security checks must be done BEFORE calling the following methods!

    long createRoom(String name, long userId);
    boolean deleteRoom(long roomId);
    boolean changeRoomName(String name, long roomId);

    //force = bypass invitation logic, forceExit = if the user is in another room, add him to the current one forcefully
    long invitePersonToRoom(long invitedUserId, long roomId, boolean force, boolean forceExit);
    boolean inviteAccept(long guestId);
    boolean inviteRefuse(long guestId);
    boolean inviteCancel(long guestId);

    boolean kickFromRoom(long guestId);
    boolean leaveRoom(long guestId);

    boolean confirmRoom(long roomId);
    boolean unconfirmRoom(long roomId);

    void doSanityChecks();
}
