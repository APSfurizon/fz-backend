package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.response.ListRoomPricesAvailabilityResponse;
import net.furizon.backend.feature.room.dto.response.RoomAvailabilityInfoResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListRoomWithPricesAndQuotaUseCase implements
        UseCase<ListRoomWithPricesAndQuotaUseCase.Input, ListRoomPricesAvailabilityResponse> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomConfig roomConfig;

    @Override
    public @NotNull ListRoomPricesAvailabilityResponse executor(
            @NotNull ListRoomWithPricesAndQuotaUseCase.Input input) {
        long userId = input.user.getUserId();
        log.debug("User {} is obtaining room quota and prices", userId);
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        RoomData currentRoomData = roomFinder.getRoomDataForUser(userId, event, pretixInformation);
        Long currentRoomPrice = currentRoomData == null ? null : pretixInformation.getRoomPriceByItemId(currentRoomData.getRoomPretixItemId(), false);

        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean editingTimeAllowed = endRoomEditingTime == null || endRoomEditingTime.isAfter(OffsetDateTime.now());
        boolean buyOrUpgradeSupported = editingTimeAllowed && roomLogic.isRoomBuyOrUpgradeSupported(event);

        Set<Long> roomIds = pretixInformation.getRoomPretixIds();
        //Return empty list if not buyOrUpgradeSupported
        List<RoomAvailabilityInfoResponse> rooms = buyOrUpgradeSupported ? roomIds.stream().map(id -> {
            Long price = pretixInformation.getRoomPriceByItemId(id, false);
            if (price != null && (currentRoomPrice == null || price >= currentRoomPrice)) {

                RoomData data = roomFinder.getRoomDataFromPretixItemId(id, pretixInformation);
                var r = pretixInformation.getAvailabilityFromItemId(id);

                if (r.isPresent() && data != null) {
                    return new RoomAvailabilityInfoResponse(
                        data,
                        PretixGenericUtils.fromPriceToString(price, '.'),
                        r.get()
                    );
                }
            }
            return null;
        }).filter(Objects::nonNull).toList() : List.of();

        return new ListRoomPricesAvailabilityResponse(
                currentRoomData,
                currentRoomPrice == null ? null : PretixGenericUtils.fromPriceToString(currentRoomPrice, '.'),
                rooms
        );
    }

    public record Input(
        FurizonUser user,
        PretixInformation pretixInformation
    ) {}
}
