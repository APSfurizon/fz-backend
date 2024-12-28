package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.response.ListRoomPricesAvailabilityResponse;
import net.furizon.backend.feature.room.dto.response.RoomAvailabilityInfoResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListRoomWithPricesAndQuotaUseCase implements
        UseCase<ListRoomWithPricesAndQuotaUseCase.Input, ListRoomPricesAvailabilityResponse> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomConfig roomConfig;
    @NotNull private final RoomChecks roomChecks;

    @Override
    public @NotNull ListRoomPricesAvailabilityResponse executor(
            @NotNull ListRoomWithPricesAndQuotaUseCase.Input input) {
        long userId = input.user.getUserId();
        log.debug("User {} is obtaining room quota and prices", userId);
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        //Check if we actually can buy or upgrade room
        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean editingTimeAllowed = endRoomEditingTime == null || endRoomEditingTime.isAfter(OffsetDateTime.now());
        boolean buyOrUpgradeSupported = editingTimeAllowed && roomLogic.isRoomBuyOrUpgradeSupported(event);

        // Fetch extraDays price
        long currentExtraDaysPaid = 0L;
        Order order = roomChecks.getOrderAndAssertItExists(userId, event, pretixInformation);
        Long pretixRoomItemId = order.getPretixRoomItemId();


        //Fetch extra days price
        ExtraDays extraDays = order.getExtraDays();
        short capacity = order.getRoomCapacity();
        String hotelInternalName = Objects.requireNonNull(order.getHotelInternalName());
        if (order.hasRoom() && buyOrUpgradeSupported) {
            if (extraDays.isEarly()) {
                long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(hotelInternalName, capacity, ExtraDays.EARLY));
                currentExtraDaysPaid += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
            }
            if (extraDays.isLate()) {
                long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(hotelInternalName, capacity, ExtraDays.LATE));
                currentExtraDaysPaid += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
            }
        }
        //Fetch room price
        Long currentRoomPrice = pretixRoomItemId == null
                ? null
                : pretixInformation.getItemPrice(pretixRoomItemId, false);
        //Fetch room guests
        Optional<Long> roomId = buyOrUpgradeSupported ? roomFinder.getRoomIdFromOwnerUserId(userId, event)
                : Optional.empty();
        List<RoomGuest> guests = roomId.map(id -> roomFinder.getRoomGuestsFromRoomId(id, true))
                .orElse(null);


        long totalPaid = (currentRoomPrice == null ? 0L : currentRoomPrice) + currentExtraDaysPaid;
        Set<Long> roomIds = pretixInformation.getRoomPretixIds();
        //Return empty list if not buyOrUpgradeSupported
        List<RoomAvailabilityInfoResponse> rooms = buyOrUpgradeSupported ? roomIds.stream().map(id -> {
            //Check for room capacity > number of people already in room
            HotelCapacityPair roomInfo = pretixInformation.getRoomInfoFromPretixItemId(id);
            if (roomInfo != null && (guests == null || roomInfo.capacity() >= guests.size())) {
                //Check for roomPrice > old room roomPrice
                // (actual check against how much the user has paid is done on the actual action)
                long roomPrice = Objects.requireNonNull(pretixInformation.getItemPrice(id, false));
                long extraDaysPrice = 0L;
                if (order.hasRoom()) {
                    if (extraDays.isEarly()) {
                        long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(roomInfo, ExtraDays.EARLY));
                        extraDaysPrice += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
                    }
                    if (extraDays.isLate()) {
                        long extraDayItemId = Objects.requireNonNull(pretixInformation.getExtraDayItemIdForHotelCapacity(roomInfo, ExtraDays.LATE));
                        extraDaysPrice += Objects.requireNonNull(pretixInformation.getItemPrice(extraDayItemId, false));
                    }
                }
                long totalPrice = roomPrice + extraDaysPrice;

                if (totalPrice >= totalPaid) {
                    //Fetch availability
                    RoomData data = roomFinder.getRoomDataFromPretixItemId(id, pretixInformation);
                    var r = pretixInformation.getSmallestAvailabilityFromItemId(id);
                    //TODO get smallest availability between room, early and late

                    if (r.isPresent() && data != null) {
                        return new RoomAvailabilityInfoResponse(
                            data,
                            PretixGenericUtils.fromPriceToString(roomPrice, '.'),
                            r.get()
                        );
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).toList() : List.of();

        return new ListRoomPricesAvailabilityResponse(
                order.getBoughtRoomData(pretixInformation),
                currentRoomPrice == null ? null : PretixGenericUtils.fromPriceToString(currentRoomPrice, '.'),
                rooms
        );
    }

    public record Input(
        FurizonUser user,
        PretixInformation pretixInformation
    ) {}
}
