package net.furizon.backend.feature.room.logic;

import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RoomLogic {
    //Security checks must be done BEFORE calling the following methods!

    boolean canCreateRoom(long userId, @NotNull Event event);
    long createRoom(String name, long userId, @NotNull Event event);
    boolean deleteRoom(long roomId);
    boolean changeRoomName(String name, long roomId);
    boolean setShowInNosecount(boolean showInNosecount, long roomId);

    //force = bypass invitation logic, forceExit = if the user is in another room, add him to the current one forcefully
    long invitePersonToRoom(long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit);
    boolean inviteAccept(long guestId, long invitedUserId, long roomId, @NotNull Event event);
    boolean inviteRefuse(long guestId);
    boolean inviteCancel(long guestId);

    boolean kickFromRoom(long guestId);
    boolean leaveRoom(long guestId);

    boolean isConfirmationSupported();
    boolean canConfirmRoom(long roomId, @NotNull Event event);
    boolean confirmRoom(long roomId);
    boolean isUnconfirmationSupported();
    boolean canUnconfirmRoom(long roomId);
    boolean unconfirmRoom(long roomId);

    boolean exchangeRoom(long targetUsrId, long sourceUsrId, @Nullable Long targetRoomId, @Nullable Long sourceRoomId,
                         @NotNull Event event, @NotNull PretixInformation pretixInformation);
    boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event,
                              @NotNull PretixInformation pretixInformation);
    boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);
    boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);

    boolean isRoomBuyOrUpgradeSupported(@NotNull Event even);
    boolean buyOrUpgradeRoom(long newRoomItemId,
                             long newRoomPrice,
                             @Nullable Long oldRoomPaid,
                             long userId,
                             @Nullable Long roomId,
                             @Nullable Long newEarlyItemId,
                             @Nullable Long newEarlyPrice,
                             @Nullable Long oldEarlyPaid,
                             @Nullable Long newLateItemId,
                             @Nullable Long newLatePrice,
                             @Nullable Long oldLatePaid,
                             @NotNull Order order,
                             @NotNull Event event,
                             @NotNull PretixInformation pretixInformation
    );

    //The logic of these two methods should be the same
    @Nullable ExtraDays getExtraDaysForUser(long userId, long eventId);
    void computeNosecountExtraDays(@NotNull NosecountRoom room);

    void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation,
                        @Nullable List<String> detectedErrors);
}
