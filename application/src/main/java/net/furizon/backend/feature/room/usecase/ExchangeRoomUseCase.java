package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.rooms.RoomEmailTexts;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.furizon.backend.infrastructure.email.EmailVars.EXCHANGE_ACTION_TEXT;
import static net.furizon.backend.infrastructure.email.EmailVars.LINK;
import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_EXCHANGE_COMPLETED;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_HAS_NEW_OWNER;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRoomUseCase implements UseCase<ExchangeRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final FrontendConfig frontendConfig;

    //IMPORTANT: This useCase doesn't care about the confirmation flow. It should be done prior to this call!
    @Override
    public @NotNull Boolean executor(@NotNull ExchangeRoomUseCase.Input input) {
        log.info("[ROOM_EXCHANGE] User {} is trying a room exchange", input.user.getUserId());
        long sourceUserId = roomChecks.getUserIdAssertPermissionCheckTimeframe(input.req.getSourceUserId(), input.user);
        long destUserId = input.req.getDestUserId();
        Event event = input.pretixInformation.getCurrentEvent();

        generalChecks.assertUserHasOrderAndItsNotDaily(sourceUserId, event);
        generalChecks.assertUserHasOrderAndItsNotDaily(destUserId, event);

        //We can exchange room with someone else
        //checks.assertUserHasNotBoughtAroom(destUserId, event);
        roomChecks.assertUserHasBoughtAroom(sourceUserId, event);

        roomChecks.assertUserIsNotInRoom(destUserId, event, true);

        //checks.assertUserDoesNotOwnAroom(destUserId, event);
        var sourceRoom = roomFinder.getRoomIdFromOwnerUserId(sourceUserId, event);
        sourceRoom.ifPresent(id -> {
            roomChecks.assertPermissionsOnRoom(sourceUserId, event, id, null);
            roomChecks.assertRoomNotConfirmed(id);
        });

        var destRoom = roomFinder.getRoomIdFromOwnerUserId(destUserId, event);
        destRoom.ifPresent(roomChecks::assertRoomNotConfirmed);

        Order sourceOrder = generalChecks.getOrderAndAssertItExists(sourceUserId, event, input.pretixInformation);
        Order targetOrder = generalChecks.getOrderAndAssertItExists(destUserId, event, input.pretixInformation);

        generalChecks.assertOrderIsPaid(sourceOrder, sourceUserId, event);
        generalChecks.assertOrderIsPaid(targetOrder, destUserId, event);

        generalChecks.assertPaymentAndRefundConfirmed(sourceOrder.getCode(), event);
        generalChecks.assertPaymentAndRefundConfirmed(targetOrder.getCode(), event);

        if (input.runOnlyChecks) {
            return true;
        }

        boolean res = roomLogic.exchangeRoom(
                destUserId, sourceUserId,
                destRoom.orElse(null), sourceRoom.orElse(null),
                event, input.pretixInformation
        );
        if (res) {
            UserEmailData destData = userFinder.getMailDataForUser(destUserId);
            UserEmailData sourceData = userFinder.getMailDataForUser(sourceUserId);
            if (destData != null) {
                List<MailRequest> mails = new ArrayList<>(16);
                if (sourceRoom.isPresent()) {
                    mails.addAll(mailService.prepareBroadcastUpdate(
                            sourceRoom.get(), TEMPLATE_ROOM_HAS_NEW_OWNER,
                            MailVarPair.of(ROOM_OWNER_FURSONA_NAME, destData.getFursonaName()),
                            MailVarPair.of(LINK, frontendConfig.getRoomPageUrl())
                    ));
                }

                if (sourceData != null) {
                    String actionText = RoomEmailTexts.getActionText(input.req.getAction(), destRoom.isPresent());
                    mails.addAll(mailService.prepareUpdate(
                        new MailRequest(
                            destData, TEMPLATE_EXCHANGE_COMPLETED,
                            MailVarPair.of(EXCHANGE_ACTION_TEXT, actionText),
                            MailVarPair.of(OTHER_FURSONA_NAME, sourceData.getFursonaName()),
                            MailVarPair.of(LINK, frontendConfig.getReservationPageUrl())
                        ),
                        new MailRequest(
                            sourceData, TEMPLATE_EXCHANGE_COMPLETED,
                            MailVarPair.of(EXCHANGE_ACTION_TEXT, actionText),
                            MailVarPair.of(OTHER_FURSONA_NAME, destData.getFursonaName()),
                            MailVarPair.of(LINK, frontendConfig.getReservationPageUrl())
                        )
                    ));
                }
                mailService.fireAndForgetMany(mails);
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
