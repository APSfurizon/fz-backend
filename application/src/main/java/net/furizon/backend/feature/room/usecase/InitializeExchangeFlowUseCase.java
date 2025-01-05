package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.action.createExchangeConfirmationStatusObj.CreateExchangeObjAction;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.furizon.backend.infrastructure.email.EmailVars.*;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeExchangeFlowUseCase implements UseCase<InitializeExchangeFlowUseCase.Input, Boolean> {
    @NotNull private final CreateExchangeObjAction createExchangeObjAction;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    @Value("${frontend.transfer-exchange-confirmation-url}")
    private String transferExchangeConfirmationUrl;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        Event event = input.event;
        long recipientUserId = input.req.getRecipientUserId();
        ExchangeAction action = input.req.getAction();
        log.info("{} is initializing a {} exchange with target user {} ",
                input.user.getUserId(), action, recipientUserId);

        long sourceUserId = checks.getUserIdAndAssertPermission(input.req.getSourceUserId(), input.user);
        checks.assertSourceUserHasNotPendingExchanges(sourceUserId, input.event);

        log.info("Init {} exchange: {} -> {}", action, sourceUserId, recipientUserId);
        long exchangeId  = createExchangeObjAction.invoke(recipientUserId, sourceUserId, action, event);

        UserEmailData recipientData = userFinder.getMailDataForUser(recipientUserId);
        UserEmailData sourceData = userFinder.getMailDataForUser(sourceUserId);
        if (recipientData != null && sourceData != null) {
            boolean recipientHasRoom = false;
            if (action == ExchangeAction.TRASFER_EXCHANGE_ROOM) {
                var r = orderFinder.userHasBoughtAroom(recipientUserId, event);
                recipientHasRoom = r.isPresent() && r.get();
            }
            String actionText = switch (action) {
                case TRASFER_EXCHANGE_ROOM -> TRANSFER_FULL_ORDER;
                case TRASFER_FULL_ORDER -> recipientHasRoom ? EXCHANGE_ROOM : TRANSFER_ROOM;
            };
            MailVarPair[] vars = {
                new MailVarPair(EXCHANGE_ACTION_TEXT, actionText),
                new MailVarPair(OWNER_FURSONA_NAME, sourceData.getFursonaName()),
                new MailVarPair(FURSONA_NAME, recipientData.getFursonaName()),
                new MailVarPair(EXCHANGE_LINK, transferExchangeConfirmationUrl + exchangeId),
            };
            mailService.sendUpdate(recipientUserId, TITLE_RESERVATION_UPDATED, BODY_EXCHANGE_INITIALIZED, vars);
            mailService.sendUpdate(sourceUserId, TITLE_RESERVATION_UPDATED, BODY_EXCHANGE_INITIALIZED, vars);
            return true;
        }
        return false;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ExchangeRequest req,
            @NotNull Event event
    ) {}
}
