package net.furizon.backend.feature.room.usecase;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.*;
import static net.furizon.backend.infrastructure.email.EmailVars.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeFullOrderUseCase implements UseCase<ExchangeFullOrderUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    //IMPORTANT: This useCase doesn't care about the confirmation flow. It should be done prior to this call!
    @Override
    public @NotNull Boolean executor(@NotNull ExchangeFullOrderUseCase.Input input) {
        log.info("[ROOM_EXCHANGE] User {} is trying a full order exchange", input.user.getUserId());
        long sourceUserId = checks.getUserIdAndAssertPermission(input.req.getSourceUserId(), input.user);
        long destUserId = input.req.getDestUserId();
        Event event = input.pretixInformation.getCurrentEvent();


        checks.assertInTimeframeToEditRooms();

        checks.assertUserHasOrder(sourceUserId, event);
        checks.assertUserHasNotAnOrder(destUserId, event);

        long roomId = -1L;
        var srcRoomId = roomFinder.getRoomIdFromOwnerUserId(sourceUserId, event);
        if (srcRoomId.isPresent()) {
            roomId = checks.getRoomIdAndAssertPermissionsOnRoom(sourceUserId, event, null);
            checks.assertRoomNotConfirmed(roomId);
        }

        checks.assertOrderIsPaid(sourceUserId, event);

        if (input.runOnlyChecks) {
            return true;
        }

        boolean res = roomLogic.exchangeFullOrder(destUserId, sourceUserId, roomId, event, input.pretixInformation);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(destUserId);
            if (data != null) {
                mailService.broadcastUpdate(roomId, TITLE_ROOM_UPDATED, BODY_ROOM_HAS_NEW_OWNER, new MailVarPair(OWNER_FURSONA_NAME, data.getFursonaName()));
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ExchangeRequest req,
            @NotNull PretixInformation pretixInformation,
            boolean runOnlyChecks
    ) {}
}
