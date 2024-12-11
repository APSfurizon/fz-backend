package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRefuseUseCase implements UseCase<InviteRefuseUseCase.Input, Boolean> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final CommonRoomChecks commonChecks;

    @Override
    public @NotNull Boolean executor(@NotNull InviteRefuseUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;

        commonChecks.runCommonChecks(requesterUserId, event);

        RoomGuestResponse guest = commonChecks.getAndCheckRoomGuestFromId(guestId);
        if (guest.isConfirmed()) {
            log.error("Cannot refuse the already accepted invitation {}", guestId);
            throw new ApiException("Cannot refuse an already accepted invitation");
        }
        long roomId = guest.getRoomId();

        commonChecks.isRoomAlreadyConfirmedCheck(roomId);
        commonChecks.userAlreadyOwnsAroomCheck(requesterUserId, event);

        roomLogic.inviteRefuse(guestId);

        return true;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
