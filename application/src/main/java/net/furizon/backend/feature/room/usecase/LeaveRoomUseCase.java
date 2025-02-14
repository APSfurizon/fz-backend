package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_USER_LEFT_ROOM;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveRoomUseCase implements UseCase<LeaveRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull LeaveRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        RoomGuest guest = checks.getRoomGuestObjFromUserEventAndAssertItExistsAndConfirmed(
                requesterUserId,
                event
        );
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();
        long targetUserId = guest.getUserId();

        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(targetUserId, roomId);
        checks.assertIsGuestObjOwnerOrAdmin(guest, requesterUserId);
        checks.assertRoomFromCurrentEvent(roomId, event);

        boolean res = roomLogic.leaveRoom(guest.getGuestId());
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(targetUserId);
            var r = roomFinder.getOwnerUserIdFromRoomId(roomId);
            if (data != null && r.isPresent()) {
                mailService.prepareAndSendUpdate(new MailRequest(
                        r.get(), userFinder, TEMPLATE_USER_LEFT_ROOM,
                        MailVarPair.of(OTHER_FURSONA_NAME, data.getFursonaName())
                ));
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
