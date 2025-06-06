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
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        long userId = input.userId;
        Event event = input.event;
        RoomInfo info = roomFinder.getRoomInfoForUser(userId, event, input.pretixInformation, roomLogic);

        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean editingTimeAllowed = input.ignoreEditingTime
                                  || endRoomEditingTime == null
                                  || endRoomEditingTime.isAfter(OffsetDateTime.now());

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
            //By sorting we normally should have the owner of the room in the first position*
            // *Except when the room has changed owner for some reaons. In this instance,
            //  it's fine if it's not the first one
            RoomGuestResponse[] arr = guests.toArray(new RoomGuestResponse[guests.size()]);
            Arrays.sort(arr, (g1, g2) -> (int) (g1.getRoomGuest().getGuestId() - g2.getRoomGuest().getGuestId()));
            List<RoomGuestResponse> sortedGuests = new ArrayList<>(arr.length);
            //Include first the confirmed guests, later the invitations
            sortedGuests.addAll(Arrays.stream(arr).filter(g -> g.getRoomGuest().isConfirmed()).toList());
            sortedGuests.addAll(Arrays.stream(arr).filter(g -> !g.getRoomGuest().isConfirmed()).toList());
            info.setGuests(sortedGuests);
        }
        List<RoomInvitationResponse> invitations =
            roomFinder.getUserReceivedInvitations(userId, event, input.pretixInformation, roomLogic);
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

        return new RoomInfoResponse(info, hasOrder, editingTimeAllowed, canCreateRoom, buyOrUpgradeSupported,
                canBuyOrUpgrade, canExchange, endRoomEditingTime, invitations);
    }

    public record Input(
            long userId,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation,
            boolean ignoreEditingTime
    ) {}
}
