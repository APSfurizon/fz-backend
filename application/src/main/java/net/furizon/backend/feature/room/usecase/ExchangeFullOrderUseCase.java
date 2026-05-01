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
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.LINK;
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
    @NotNull private final FrontendConfig frontendConfig;
    @NotNull private final TranslationService translationService;

    //IMPORTANT: This useCase doesn't care about the confirmation flow. It should be done prior to this call!
    @Override
    public @NotNull Boolean executor(@NotNull ExchangeFullOrderUseCase.Input input) {
        log.info("[ROOM_EXCHANGE] User {} is trying a full order exchange", input.user.getUserId());
        Event event = input.pretixInformation.getCurrentEvent();

        long reqUserId = input.user.getUserId();
        long destUserId = input.req.getDestUserId();
        long sourceUserId = input.req.getSourceUserId() == null ? reqUserId : input.req.getSourceUserId();
        boolean isAdmin = roomChecks.isUserAdmin(reqUserId);

        if (reqUserId != destUserId && reqUserId != sourceUserId && !isAdmin) {
            log.error("User is not an admin! It cannot operate on exchange {} owned by {} -> {}",
                    input.req.getAction(), sourceUserId, destUserId);
            throw new ApiException(translationService.error("room.edit_denied"),
                    GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
        roomChecks.assertInTimeframeToEditRoomsAllowAdmin(reqUserId, input.req.getSourceUserId(), isAdmin);

        Order sourceOrder = generalChecks.getOrderAndAssertItExists(sourceUserId, event, input.pretixInformation);
        generalChecks.assertUserHasNotAnOrder(destUserId, event);

        //Check if the user owns a room
        long roomId = roomFinder.getRoomIdFromOwnerUserId(sourceUserId, event).orElse(-1L);
        if (roomId < 0L) {
            //If not, check if the user is in a room
            // We prefer doing two different checks just to be sure in case there are bugs where the following
            // doesn't return a roomId even if the user is the owner of a room
            var roomGuest = roomFinder.getConfirmedRoomGuestFromUserEvent(sourceUserId, event);
            if (roomGuest.isPresent()) {
                roomId = roomGuest.get().getRoomId();
            }
        }
        if (roomId >= 0L) {
            roomChecks.assertRoomNotConfirmed(roomId);
        }

        generalChecks.assertOrderIsPaid(sourceOrder, sourceUserId, event);
        //At least for UserBuysFullRoom logic, this check is done already inside pretix
        //generalChecks.assertPaymentAndRefundConfirmed(sourceOrder.getCode(), event);

        if (input.runOnlyChecks) {
            //Copy pasted from before
            generalChecks.assertPaymentAndRefundConfirmed(sourceOrder.getCode(), event);
            return true;
        }

        boolean res = roomLogic.exchangeFullOrder(destUserId, sourceUserId, roomId, event, input.pretixInformation);
        if (res) {
            UserEmailData data = userFinder.getMailDataForUser(destUserId);
            if (data != null) {
                mailService.prepareAndSendBroadcastUpdate(
                        roomId,
                        TEMPLATE_ROOM_HAS_NEW_OWNER,
                        TranslatableValue.ofEmail("mail.room_has_new_owner.title"),
                        MailVarPair.of(ROOM_OWNER_FURSONA_NAME, data.getFursonaName()),
                        MailVarPair.of(LINK, frontendConfig.getRoomPageUrl())
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
