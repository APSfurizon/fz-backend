package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetRoomInfoUseCase implements UseCase<GetRoomInfoUseCase.Input, RoomInfoResponse> {
    @NotNull private final ExchangeConfirmationFinder exchangeConfirmationFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomConfig roomConfig;

    @Override
    public @NotNull RoomInfoResponse executor(@NotNull GetRoomInfoUseCase.Input input) {
        long userId = input.user.getUserId();
        Event event = input.event;
        RoomInfo info = roomFinder.getRoomInfoForUser(userId, event, input.pretixInformation);

        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean editingTimeAllowed = endRoomEditingTime == null || endRoomEditingTime.isAfter(OffsetDateTime.now());

        boolean isOwner = true; //By defaulting it on true, we can upgrade room also if we don't have a room
        if (info != null) {
            isOwner = info.getRoomOwner().getUserId() == userId;
            long roomId = info.getRoomId();
            info.setUserIsOwner(isOwner);
            info.setCanConfirm(isOwner && editingTimeAllowed
                               && !info.isConfirmed() && roomLogic.canConfirmRoom(roomId, event));
            info.setCanUnconfirm(isOwner && editingTimeAllowed
                               && info.isConfirmed() && roomLogic.canUnconfirmRoom(roomId));
            info.setConfirmationSupported(isOwner && roomLogic.isConfirmationSupported());
            info.setUnconfirmationSupported(isOwner && roomLogic.isUnconfirmationSupported());

            List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, event);

            info.setCanInvite(isOwner && editingTimeAllowed && (
                    //guests.stream().filter(g -> g.getRoomGuest().isConfirmed()).count()
                    guests.size() //Counting also unconfirmed invites to prevent mass spam
                    < (int) info.getRoomData().getRoomCapacity()
                )
            );
            info.setGuests(guests);
        }
        List<RoomInvitationResponse> invitations =
            roomFinder.getUserReceivedInvitations(userId, event, input.pretixInformation);
        for (RoomInvitationResponse invitation : invitations) {
            var guests = roomFinder.getRoomGuestResponseFromRoomId(invitation.getRoom().getRoomId(), event);
            invitation.getRoom().setGuests(guests);
        }

        Optional<Boolean> r = orderFinder.isOrderDaily(userId, event);
        boolean hasOrder = r.isPresent();

        boolean canCreateRoom = editingTimeAllowed && hasOrder
                && info == null && roomLogic.canCreateRoom(userId, event);
        boolean buyOrUpgradeSupported = roomLogic.isRoomBuyOrUpgradeSupported(event);

        boolean canExchange = editingTimeAllowed && isOwner && hasOrder
                && exchangeConfirmationFinder.getExchangeStatusFromSourceUsrIdEvent(userId, event) == null;
        boolean canBuyOrUpgrade = canExchange && buyOrUpgradeSupported;

        log.debug("RoomInfo: info={} userid={} event={} editingTimeAllowed={} isOwner={} hasOrder={}",
                info, userId, event, editingTimeAllowed, isOwner, hasOrder);

        return new RoomInfoResponse(info, hasOrder, canCreateRoom, buyOrUpgradeSupported,
                canBuyOrUpgrade, canExchange, endRoomEditingTime, invitations);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
