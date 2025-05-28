package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.action.confirmUserExchangeStatus.ConfirmUserExchangeStatusAction;
import net.furizon.backend.feature.room.action.deleteExchangeStatusObjAction.DeleteExchangeStatusObjAction;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.dto.request.UpdateExchangeStatusRequest;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateExchangeStatusUseCase implements
        UseCase<UpdateExchangeStatusUseCase.Input, ExchangeConfirmationStatus> {
    @NotNull private final ConfirmUserExchangeStatusAction confirm;
    @NotNull private final DeleteExchangeStatusObjAction delete;
    @NotNull private final ExchangeConfirmationFinder finder;
    @NotNull private final RoomChecks checks;

    @Override
    public @NotNull ExchangeConfirmationStatus executor(@NotNull Input input) {
        long exchangeId = input.req.getExchangeId();
        boolean toConfirm = input.req.getConfirm();
        long userId = input.targetUserId;
        log.info("[ROOM_EXCHANGE] User {} is updating confirmation status on exchange {}: Status = {}",
                userId, exchangeId, toConfirm);

        checks.assertInTimeframeToEditRoomsAllowAdmin(input.user.getUserId(), userId, null);
        ExchangeConfirmationStatus status = finder.getExchangeStatusFromId(exchangeId);
        checks.assertExchangeExist(status, exchangeId);
        checks.assertUserHasRightsOnExchange(userId, status);

        if (toConfirm) {
            boolean isSourceUser = userId == status.getSourceUserId();
            boolean otherSideAlreadyConfirmed = isSourceUser ? status.isTargetConfirmed() : status.isSourceConfirmed();

            if (otherSideAlreadyConfirmed) {
                //If the other side has already confirmed, the flow is done:
                // we delete the confirmation entry and we return true
                // to say to the caller we can continue with the operation
                log.info("[ROOM_EXCHANGE] {}: Both parties have confirmed!", exchangeId);
                delete.invoke(exchangeId);
                status.confirmUser(isSourceUser);
            } else {
                log.info("[ROOM_EXCHANGE] {}: User {} has confirmed the exchange",
                        exchangeId, userId);
                status.confirmUser(isSourceUser, confirm);
            }
        } else {
            log.info("[ROOM_EXCHANGE] {}: User {} has rejected the exchange. Deleting it...",
                    exchangeId, userId);
            delete.invoke(exchangeId);
            status.unconfirmAll();
        }

        return status;
    }

    public record Input(
            @NotNull FurizonUser user,
            //Permission on targetUserId MUST be checked in prior!!
            long targetUserId,
            @NotNull UpdateExchangeStatusRequest req
    ) {}
}
