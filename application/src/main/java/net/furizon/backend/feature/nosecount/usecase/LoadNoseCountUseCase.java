package net.furizon.backend.feature.nosecount.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.JooqNosecountObj;
import net.furizon.backend.feature.nosecount.dto.NosecountHotel;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.nosecount.dto.NosecountRoomType;
import net.furizon.backend.feature.nosecount.dto.responses.NoseCountResponse;
import net.furizon.backend.feature.nosecount.finder.CountsFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class LoadNoseCountUseCase implements UseCase<LoadNoseCountUseCase.Input, NoseCountResponse> {
    @NotNull private final CountsFinder countsFinder;
    @NotNull private final RoomConfig roomConfig;

    @Override
    public @NotNull NoseCountResponse executor(@NotNull LoadNoseCountUseCase.Input input) {
        if (input.event == null) {
            throw new ApiException("Event is null", GeneralResponseCodes.EVENT_NOT_FOUND);
        }
        OffsetDateTime from = input.event.getDateFrom();

        Map<LocalDate, List<UserDisplayData>> dailys = new TreeMap<>();
        List<UserDisplayData> roomless = new ArrayList<>();
        Map<Long, NosecountRoom> roomIdToRoom = new HashMap<>();
        Map<Long, NosecountRoomType> roomPretixItemIdToRoomType = new HashMap<>();
        Map<String, NosecountHotel> hotelInternalNameToHotel = new HashMap<>();

        List<JooqNosecountObj> data = countsFinder.getNosecount(input.event.getId());
        for (JooqNosecountObj obj : data) {

            //Daily days furs
            long dailyDays = obj.getDailyDays();
            if (dailyDays != 0L) {
                if (from != null) {
                    Order.parseDailyDays(dailyDays).forEach(day -> addDaily(dailys, obj, day, from));
                }
                continue;
            }

            //Roomless furs
            if (obj.getRoomId() == null) {
                roomless.add(getUserDisplayData(obj));
                continue;
            }

            //Roomed furs
            //Fetch or create room
            NosecountRoom room = roomIdToRoom.computeIfAbsent(obj.getRoomId(), roomId -> {
                NosecountRoom r = new NosecountRoom(
                        roomId, Objects.requireNonNull(obj.getRoomName()), new ArrayList<>()
                );

                //Fetch or create room type
                NosecountRoomType roomType = roomPretixItemIdToRoomType.computeIfAbsent(obj.getRoomPretixItemId(),
                    itemId -> {
                        Objects.requireNonNull(itemId);
                        NosecountRoomType rt = new NosecountRoomType(
                            new RoomData(
                                Objects.requireNonNull(obj.getRoomCapacity()),
                                itemId,
                                obj.getRoomInternalName(),
                                input.pretixInformation.getRoomNamesFromRoomPretixItemId(itemId)
                            ), new ArrayList<>()
                        );

                        //Fetch or create hotel
                        NosecountHotel hotel = hotelInternalNameToHotel.computeIfAbsent(obj.getHotelInternalName(),
                            hotelInternalName -> {
                                Objects.requireNonNull(hotelInternalName);
                                return new NosecountHotel(
                                    Objects.requireNonNull(roomConfig.getHotelNames(hotelInternalName)),
                                    hotelInternalName,
                                    new ArrayList<>()
                                );
                            });
                        hotel.getRoomTypes().add(rt);
                        return rt;
                    }
                );
                roomType.getRooms().add(r);
                return r;
            });

            room.getGuests().add(getUserDisplayData(obj));
        }


        return new NoseCountResponse(
            hotelInternalNameToHotel.values().stream().toList(),
            roomless,
            dailys
        );
    }

    private void addDaily(@NotNull Map<LocalDate, List<UserDisplayData>> dailys,
                          @NotNull JooqNosecountObj obj, int dayNo, @NotNull OffsetDateTime from) {
        LocalDate day = from.plusDays((long) dayNo).toLocalDate();
        List<UserDisplayData> l = dailys.computeIfAbsent(day, d -> new ArrayList<>());
        l.add(getUserDisplayData(obj));
    }

    private @NotNull UserDisplayData getUserDisplayData(@NotNull JooqNosecountObj obj) {
        return new UserDisplayData(
                obj.getUserId(),
                obj.getFursonaName(),
                obj.getUserLocale(),
                obj.getMedia(),
                obj.getSponsorship()
        );
    }

    public record Input(
            @Nullable Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
