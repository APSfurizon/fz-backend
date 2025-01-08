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

import static net.furizon.backend.infrastructure.email.EmailVars.EXCHANGE_ACTION_TEXT;
import static net.furizon.backend.infrastructure.email.EmailVars.EXCHANGE_LINK;
import static net.furizon.backend.infrastructure.email.EmailVars.FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.EXCHANGE_ROOM;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_EXCHANGE_INITIALIZED;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TRANSFER_FULL_ORDER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TRANSFER_ROOM;

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
        long destUserId = input.req.getDestUserId();
        ExchangeAction action = input.req.getAction();
        log.info("{} is initializing a {} exchange with target user {} ",
                input.user.getUserId(), action, destUserId);

        long sourceUserId = checks.getUserIdAndAssertPermission(input.req.getSourceUserId(), input.user);
        checks.assertSourceUserHasNotPendingExchanges(sourceUserId, input.event);

        log.info("Init {} exchange: {} -> {}", action, sourceUserId, destUserId);
        long exchangeId  = createExchangeObjAction.invoke(destUserId, sourceUserId, action, event);

        UserEmailData destData = userFinder.getMailDataForUser(destUserId);
        UserEmailData sourceData = userFinder.getMailDataForUser(sourceUserId);
        if (destData != null && sourceData != null) {
            boolean destHasRoom = false;
            if (action == ExchangeAction.TRASFER_EXCHANGE_ROOM) {
                var r = orderFinder.userHasBoughtAroom(destUserId, event);
                destHasRoom = r.isPresent() && r.get();
            }
            String actionText = switch (action) {
                case TRASFER_EXCHANGE_ROOM -> TRANSFER_FULL_ORDER;
                case TRASFER_FULL_ORDER -> destHasRoom ? EXCHANGE_ROOM : TRANSFER_ROOM;
            };
            MailVarPair[] vars = {
                new MailVarPair(EXCHANGE_ACTION_TEXT, actionText),
                new MailVarPair(OWNER_FURSONA_NAME, sourceData.getFursonaName()),
                new MailVarPair(FURSONA_NAME, destData.getFursonaName()),
                new MailVarPair(EXCHANGE_LINK, transferExchangeConfirmationUrl + exchangeId),
            };
            mailService.sendUpdate(destUserId, TEMPLATE_EXCHANGE_INITIALIZED, vars);
            mailService.sendUpdate(sourceUserId, TEMPLATE_EXCHANGE_INITIALIZED, vars);
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
