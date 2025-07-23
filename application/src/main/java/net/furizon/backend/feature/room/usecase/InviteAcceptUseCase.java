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
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_INVITE_ACCEPTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteAcceptUseCase implements UseCase<InviteAcceptUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        long guestId = input.req.getGuestId();
        Event event = input.event;


        RoomGuest guest = roomChecks.getRoomGuestAssertPermissionCheckTimeframe(guestId, requesterUserId);
        long roomId = guest.getRoomId();
        long targetUserId = guest.getUserId();

        roomChecks.assertRoomNotConfirmed(roomId);
        roomChecks.assertGuestIsNotConfirmed(guest);
        roomChecks.assertRoomFromCurrentEvent(roomId, event);
        generalChecks.assertOrderIsPaid(targetUserId, event);
        roomChecks.assertUserIsNotInRoom(targetUserId, event, false);
        roomChecks.assertUserIsNotRoomOwner(targetUserId, roomId);
        roomChecks.assertUserDoesNotOwnAroom(targetUserId, event);
        generalChecks.assertUserHasOrderAndItsNotDaily(targetUserId, event);

        boolean res = roomLogic.inviteAccept(guestId, targetUserId, roomId, event, input.pretixInformation);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(targetUserId);
            var r = roomFinder.getOwnerUserIdFromRoomId(roomId);
            if (data != null && r.isPresent()) {
                mailService.prepareAndSendUpdate(new MailRequest(
                        r.get(), userFinder, TEMPLATE_INVITE_ACCEPTED,
                        MailVarPair.of(OTHER_FURSONA_NAME, data.getFursonaName())
                ));
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull GuestIdRequest req,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
