package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.action.createExchangeConfirmationStatusObj.CreateExchangeObjAction;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeExchangeFlowUseCase implements UseCase<InitializeExchangeFlowUseCase.Input, Boolean> {
    @NotNull private final CreateExchangeObjAction createExchangeObjAction;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        Event event = input.event;
        long destUsrId = input.req.getDestUserId();
        ExchangeAction action = input.req.getAction();
        log.info("{} is inizializing a {} exchange with target user {} ",
                input.user.getUserId(), action, destUsrId);

        long sourceUsrId = checks.getUserIdAndAssertPermission(input.req.getSourceUserId(), input.user);
        checks.assertSourceUserHasNotPendingExchanges(sourceUsrId, input.event);

        log.info("Init {} exchange: {} -> {}", action, sourceUsrId, destUsrId);
        long exchangeId  = createExchangeObjAction.invoke(destUsrId, sourceUsrId, action, event);

        //TODO send email to both users

        return true;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull ExchangeRequest req,
            @NotNull Event event
    ) {}
}
