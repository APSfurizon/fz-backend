package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KickMemberUseCase implements UseCase<KickMemberUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull KickMemberUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;

        checks.assertInTimeframeToEditRooms();
        RoomGuest guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();
        long targetUserId = guest.getUserId();

        roomId = checks.getRoomIdAndAssertPermissionsOnRoom(requesterUserId, event, roomId);
        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(targetUserId, roomId);

        boolean res = roomLogic.kickFromRoom(guestId);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(requesterUserId);
            if (data != null) {
                mailService.sendUpdate(targetUserId, TITLE_ROOM_UPDATED, BODY_KICKED_FROM_ROOM, new MailVarPair(OWNER_FURSONA_NAME, data.getFursonaName()));
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req,
            @NotNull Event event
    ) {}
}
