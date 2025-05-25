package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.usecase.UserIdRequest;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
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
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
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
    @NotNull private final RoomChecks checks;
    @NotNull private final RoomConfig roomConfig;
    @NotNull private final GeneralChecks generalChecks;

    @Override
    public @NotNull ListRoomPricesAvailabilityResponse executor(
            @NotNull ListRoomWithPricesAndQuotaUseCase.Input input) {
        Long reqUserId = input.userIdRequest == null ? null : input.userIdRequest.getUserId();
        long userId = generalChecks.getUserIdAndAssertPermission(reqUserId, input.user);
        boolean disableUnupgradeFilter = reqUserId != null;
        log.debug("User {} is obtaining room quota and prices", userId);

        //Since it's an heavy request, we prevent people from randomly doing so when not needed
        checks.assertInTimeframeToEditRooms();

        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        //Check if we actually can buy or upgrade room
        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean editingTimeAllowed = endRoomEditingTime == null || endRoomEditingTime.isAfter(OffsetDateTime.now());
        boolean buyOrUpgradeSupported = editingTimeAllowed && roomLogic.isRoomBuyOrUpgradeSupported(event);

        // Fetch extraDays price
        long currentExtraDaysPaid = 0L;
        Order order = generalChecks.getOrderAndAssertItExists(userId, event, pretixInformation);
        Long pretixRoomItemId = order.hasRoom() ? order.getPretixRoomItemId() : null;


        //Fetch extra days price
        ExtraDays extraDays = order.getExtraDays();
        short capacity = order.getRoomCapacity();
        String hotelInternalName = null;
        String roomInternalName = null;
        if (order.hasRoom() && buyOrUpgradeSupported) {
            hotelInternalName = Objects.requireNonNull(order.getHotelInternalName());
            roomInternalName = Objects.requireNonNull(order.getRoomInternalName());
            if (extraDays.isEarly()) {
                long extraDayItemId = Objects.requireNonNull(
                        pretixInformation.getExtraDayItemIdForHotelCapacity(
                                hotelInternalName, roomInternalName, capacity, ExtraDays.EARLY
                        )
                );
                currentExtraDaysPaid += Objects.requireNonNull(
                        pretixInformation.getItemPrice(extraDayItemId, false, true)
                );
            }
            if (extraDays.isLate()) {
                long extraDayItemId = Objects.requireNonNull(
                        pretixInformation.getExtraDayItemIdForHotelCapacity(
                                hotelInternalName, roomInternalName, capacity, ExtraDays.LATE
                        )
                );
                currentExtraDaysPaid += Objects.requireNonNull(
                        pretixInformation.getItemPrice(extraDayItemId, false, true)
                );
            }
        }
        //Fetch room price
        Long currentRoomPrice = pretixRoomItemId == null
                ? null
                : pretixInformation.getItemPrice(pretixRoomItemId, false, true);
        //Fetch room guests
        Optional<Long> roomId = buyOrUpgradeSupported ? roomFinder.getRoomIdFromOwnerUserId(userId, event)
                : Optional.empty();
        List<RoomGuest> guests = roomId.map(id -> roomFinder.getRoomGuestsFromRoomId(id, false))
                .orElse(null);


        long totalPaid = (currentRoomPrice == null ? 0L : currentRoomPrice) + currentExtraDaysPaid;
        Set<Long> roomItemIds = pretixInformation.getRoomPretixIds();
        //Return empty list if not buyOrUpgradeSupported
        List<RoomAvailabilityInfoResponse> rooms = buyOrUpgradeSupported ? roomItemIds.stream().map(itemId -> {
            //We exclude the current room
            if (Objects.equals(itemId, pretixRoomItemId)) {
                return null;
            }
            //Check for room capacity >= number of people already in room
            HotelCapacityPair roomInfo = pretixInformation.getRoomInfoFromPretixItemId(itemId);
            if (roomInfo != null && (guests == null || roomInfo.capacity() >= guests.size())) {
                //Check for roomPrice > old room roomPrice
                // (actual check against how much the user has paid is done on the actual action)
                long roomPrice = Objects.requireNonNull(pretixInformation.getItemPrice(itemId, false, true));
                long extraDaysPrice = 0L;
                Long earlyItemId = null;
                Long lateItemId = null;
                if (order.hasRoom()) {
                    if (extraDays.isEarly()) {
                        earlyItemId = Objects.requireNonNull(
                                pretixInformation.getExtraDayItemIdForHotelCapacity(roomInfo, ExtraDays.EARLY)
                        );
                        extraDaysPrice += Objects.requireNonNull(
                                pretixInformation.getItemPrice(earlyItemId, false, true)
                        );
                    }
                    if (extraDays.isLate()) {
                        lateItemId = Objects.requireNonNull(
                                pretixInformation.getExtraDayItemIdForHotelCapacity(roomInfo, ExtraDays.LATE)
                        );
                        extraDaysPrice += Objects.requireNonNull(
                                pretixInformation.getItemPrice(lateItemId, false, true)
                        );
                    }
                }
                long totalPrice = roomPrice + extraDaysPrice;

                if (totalPrice >= totalPaid || disableUnupgradeFilter) {
                    //Fetch availability
                    PretixQuotaAvailability quota = getSmallestQuota(
                            pretixInformation, itemId, earlyItemId, lateItemId
                    );

                    RoomData data = roomFinder.getRoomDataFromPretixItemId(itemId, pretixInformation);
                    if (quota != null && data != null) {
                        return new RoomAvailabilityInfoResponse(
                            data,
                            PretixGenericUtils.fromPriceToString(totalPrice, '.'),
                            quota.isUnlimited() ? null : quota.getRemaining()
                        );
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).toList() : Collections.emptyList();

        return new ListRoomPricesAvailabilityResponse(
                order.getBoughtRoomData(pretixInformation),
                PretixGenericUtils.fromPriceToString(totalPaid, '.'),
                rooms
        );
    }

    @Nullable
    private PretixQuotaAvailability getSmallestQuota(@NotNull PretixInformation pretixInformation, Long... items) {
        PretixQuotaAvailability ret = null;
        long minRemaining = Long.MAX_VALUE;
        for (Long item : items) {
            if (item != null) {
                var r = pretixInformation.getSmallestAvailabilityFromItemId(item);
                if (!r.isPresent()) {
                    log.warn("Unable to fetch quota for item {}", item);
                    return null;
                }
                PretixQuotaAvailability q = r.get();
                long remaining = q.getRemaining();
                if (remaining < minRemaining) {
                    minRemaining = remaining;
                    ret = q;
                }
            }
        }
        return ret;
    }

    public record Input(
        @NotNull FurizonUser user,
        @Nullable UserIdRequest userIdRequest,
        @NotNull PretixInformation pretixInformation
    ) {}
}
