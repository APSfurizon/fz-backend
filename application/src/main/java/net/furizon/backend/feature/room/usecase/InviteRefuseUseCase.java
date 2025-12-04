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
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_INVITE_REFUSE;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRefuseUseCase implements UseCase<InviteRefuseUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull InviteRefuseUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();

        RoomGuest guest = checks.getRoomGuestObjAndAssertItExists(guestId);
        checks.assertGuestIsNotConfirmed(guest);
        long roomId = guest.getRoomId();
        long targetUserId = guest.getUserId();

        //If for some reason I end up in a room with still pending invitations,
        // I want to be able to refuse them
        //checks.assertRoomNotConfirmed(roomId);
        checks.assertUserIsNotRoomOwner(guest.getUserId(), roomId);
        checks.assertIsGuestObjOwnerOrAdmin(guest, requesterUserId);
        checks.assertRoomFromCurrentEvent(roomId, input.event);

        boolean res = roomLogic.inviteRefuse(guestId);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(targetUserId);
            var r = roomFinder.getOwnerUserIdFromRoomId(roomId);
            if (data != null && r.isPresent()) {
                mailService.fireAndForget(
                    new MailRequest(
                        r.get(),
                        userFinder,
                        TEMPLATE_INVITE_REFUSE,
                        MailVarPair.of(OTHER_FURSONA_NAME, data.getFursonaName())
                    ).subject("mail.invite_refused.title", data.getFursonaName())
                );
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event,
            @NotNull GuestIdRequest req
    ) {}
}
