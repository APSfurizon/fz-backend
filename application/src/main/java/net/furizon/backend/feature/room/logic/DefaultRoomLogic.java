package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-default", matchIfMissing = true)
public class DefaultRoomLogic implements RoomLogic {
    @Override
    public long createRoom(String name, long userId) {
        return 0L;
    }

    @Override
    public boolean deleteRoom(long roomId) {
        return false;
    }

    @Override
    public boolean changeRoomName(String name, long roomId) {
        return false;
    }

    @Override
    public long invitePersonToRoom(long invitedUserId, long roomId, boolean force, boolean forceExit) {
        return 0L;
    }

    @Override
    public boolean inviteAccept(long guestId) {
        return false;
    }

    @Override
    public boolean inviteRefuse(long guestId) {
        return false;
    }

    @Override
    public boolean inviteCancel(long guestId) {
        return false;
    }

    @Override
    public boolean kickFromRoom(long guestId) {
        return false;
    }

    @Override
    public boolean leaveRoom(long guestId) {
        return false;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean unconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public void doSanityChecks() {

    }
}
