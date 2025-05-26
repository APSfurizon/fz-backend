package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_HAS_NEW_OWNER;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeFullOrderUseCase implements UseCase<ExchangeFullOrderUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final MailRoomService mailService;

    //IMPORTANT: This useCase doesn't care about the confirmation flow. It should be done prior to this call!
    @Override
    public @NotNull Boolean executor(@NotNull ExchangeFullOrderUseCase.Input input) {
        log.info("[ROOM_EXCHANGE] User {} is trying a full order exchange", input.user.getUserId());
        long sourceUserId = generalChecks.getUserIdAndAssertPermission(
                input.req.getSourceUserId(),
                input.user,
                Permission.CAN_MANAGE_ROOMS
        );
        long destUserId = input.req.getDestUserId();
        Event event = input.pretixInformation.getCurrentEvent();


        roomChecks.assertInTimeframeToEditRooms();

        Order sourceOrder = generalChecks.getOrderAndAssertItExists(sourceUserId, event, input.pretixInformation);
        generalChecks.assertUserHasNotAnOrder(destUserId, event);

        long roomId = -1L;
        var srcRoomId = roomFinder.getRoomIdFromOwnerUserId(sourceUserId, event);
        if (srcRoomId.isPresent()) {
            roomId = roomChecks.getRoomIdAndAssertPermissionsOnRoom(sourceUserId, event, null);
            roomChecks.assertRoomNotConfirmed(roomId);
        }

        generalChecks.assertOrderIsPaid(sourceOrder, sourceUserId, event);
        generalChecks.assertPaymentAndRefundConfirmed(sourceOrder.getCode(), event);

        if (input.runOnlyChecks) {
            return true;
        }

        boolean res = roomLogic.exchangeFullOrder(destUserId, sourceUserId, roomId, event, input.pretixInformation);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(destUserId);
            if (data != null) {
                mailService.prepareAndSendBroadcastUpdate(
                        roomId, TEMPLATE_ROOM_HAS_NEW_OWNER,
                        MailVarPair.of(ROOM_OWNER_FURSONA_NAME, data.getFursonaName())
                );
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
