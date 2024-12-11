package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.request.CreateRoomRequest;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRoomUseCase implements UseCase<CreateRoomUseCase.Input, RoomInfo> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final CommonRoomChecks commonChecks;

    @Override
    public @NotNull RoomInfo executor(@NotNull CreateRoomUseCase.Input input) {
        long userId = input.user.getUserId();
        Event event = input.event;

        commonChecks.userOwnsAroomCheck(userId, event);
        commonChecks.isUserInARoom(userId, event);
        commonChecks.runCommonChecks(userId, event);

        String name = input.createRoomRequest.getName();
        long roomId = roomLogic.createRoom(name, userId);

        RoomDataResponse roomData = roomFinder.getRoomDataForUser(userId, event, input.pretixInformation);

        return RoomInfo.builder()
                .roomId(roomId)
                .roomName(name)
                .roomOwnerId(userId)
                .isOwner(true)
                .confirmed(false)
                .canConfirm(roomLogic.canConfirm(roomId))
                .roomData(roomData)
                .guests(new LinkedList<>())
                .build();
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull CreateRoomRequest createRoomRequest,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
