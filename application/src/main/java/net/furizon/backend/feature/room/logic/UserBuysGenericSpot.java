package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-generic-spot")
public class UserBuysGenericSpot implements RoomLogic {
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        return r.isPresent() && !r.get() && defaultRoomLogic.canCreateRoom(userId, event);
    }

    @Override
    public long createRoom(String name, long userId, @NotNull Event event) {
        checks.assertUserHasNotBoughtAroom(userId, event);
        checks.assertUserIsNotInRoom(userId, event, false);
        return defaultRoomLogic.createRoom(name, userId, event);
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
    public boolean setShowInNosecount(boolean showInNosecount, long roomId) {
        return false;
    }

    @Override
    public long invitePersonToRoom(long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit) {
        return 0;
    }

    @Override
    public boolean inviteAccept(long guestId, long invitedUserId, long roomId, @NotNull Event event) {
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
    public boolean isConfirmationSupported() {
        return true;
    }

    @Override
    public boolean canConfirmRoom(long roomId, @NotNull Event event) {
        return false;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return false;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean unconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean isExchangeRoomSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean isExchangeFullOrderSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, @Nullable Long targetRoomId, @Nullable Long sourceRoomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Exchange of room not supported with UserBuysGenericSpot");
        return false;
    }
    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Exchange of full orders not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public boolean isRefundRoomSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean isRefundFullOrderSupported(@NotNull Event event) {
        return false;
    }
    @Override
    public boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund room not supported with UserBuysGenericSpot");
        return false;
    }
    @Override
    public boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund full order not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public boolean isRoomBuyOrUpgradeSupported(@NotNull Event even) {
        return false;
    }
    @Override
    public boolean buyOrUpgradeRoom(long newRoomItemId, long newRoomPrice, @Nullable Long oldRoomPaid, long userId, @Nullable Long roomId, @Nullable Long newEarlyItemId, @Nullable Long newEarlyPrice, @Nullable Long oldEarlyPaid, @Nullable Long newLateItemId, @Nullable Long newLatePrice, @Nullable Long oldLatePaid, @NotNull Order order, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Buy or upgrade room not supported with UserBuysGenericSpot");
        return false;
    }

    @Override
    public @Nullable ExtraDays getExtraDaysForUser(long userId, long eventId) {
        return null;
    }

    @Override
    public void computeNosecountExtraDays(@NotNull NosecountRoom room) {

    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation, @Nullable List<String> detectedErrors) {

    }
}
