package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.LINK;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_KICKED_FROM_ROOM;

@Slf4j
@Component
@RequiredArgsConstructor
public class KickMemberUseCase implements UseCase<KickMemberUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final FrontendConfig frontendConfig;

    @Override
    public @NotNull Boolean executor(@NotNull KickMemberUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;

        RoomGuest guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        checks.assertGuestIsConfirmed(guest);
        long roomId = guest.getRoomId();
        long targetUserId = guest.getUserId();

        roomId = checks.getRoomIdAssertPermissionCheckTimeframe(requesterUserId, event, roomId);
        checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(targetUserId, roomId);
        checks.assertRoomFromCurrentEvent(roomId, event);

        boolean res = roomLogic.kickFromRoom(guestId);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(requesterUserId);
            String roomName = roomFinder.getRoomName(roomId);
            if (data != null && roomName != null) {
                roomName = roomName.replaceAll("[^a-zA-Z0-9 \\\\/+\\-!?$()=]", "");
                mailService.prepareAndSendUpdate(new MailRequest(
                        targetUserId, userFinder, TEMPLATE_KICKED_FROM_ROOM,
                        MailVarPair.of(ROOM_OWNER_FURSONA_NAME, data.getFursonaName()),
                        MailVarPair.of(ROOM_NAME, roomName),
                        MailVarPair.of(LINK, frontendConfig.getRoomPageUrl())
                ));
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
