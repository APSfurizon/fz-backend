package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.response.ExchangeConfirmationStatusResponse;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetExchangeConfirmationStatusInfoUseCase implements
        UseCase<GetExchangeConfirmationStatusInfoUseCase.Input, ExchangeConfirmationStatusResponse> {
    @NotNull private final ExchangeConfirmationFinder exchangeConfirmationFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final RoomChecks checks;


    @Override
    public @NotNull ExchangeConfirmationStatusResponse executor(@NotNull Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long exchangeId = input.exchangeId;
        ExchangeConfirmationStatus status = exchangeConfirmationFinder.getExchangeStatusFromId(exchangeId);
        checks.assertExchangeExist(status, exchangeId);
        checks.assertUserHasRightsOnExchange(input.user.getUserId(), status);
        ExchangeAction action = status.getAction();

        OrderDataResponse orderData = null;
        RoomData sourceRoomData = null;
        RoomData targetRoomData = null;
        ExtraDays sourceExtraDays = null;
        ExtraDays targetExtraDays = null;
        Boolean targetRoomHidden = null;

        long sourceUserId = status.getSourceUserId();
        long targetUserId = status.getTargetUserId();
        UserDisplayData sourceUser = userFinder.getDisplayUser(sourceUserId, event);
        UserDisplayData targetUser = userFinder.getDisplayUser(targetUserId, event);

        switch (action) {
            case TRASFER_EXCHANGE_ROOM: {
                OrderDataResponse sourceResp = Objects.requireNonNull(
                        orderFinder.getOrderDataResponseFromUserEvent(sourceUserId, event, pretixInformation)
                );
                sourceRoomData = sourceResp.getRoom();
                sourceExtraDays = sourceResp.getExtraDays();

                OrderDataResponse targetResp = Objects.requireNonNull(
                        orderFinder.getOrderDataResponseFromUserEvent(targetUserId, event, pretixInformation)
                );
                boolean isSourceUser = input.user.getUserId() == status.getSourceUserId();
                if (status.isTargetConfirmed() || !isSourceUser) {
                    targetRoomData = targetResp.getRoom();
                    targetExtraDays = targetResp.getExtraDays();
                } else {
                    targetRoomHidden = targetResp.getRoom() != null;
                }
                break;
            }
            case TRASFER_FULL_ORDER: {
                orderData = Objects.requireNonNull(
                        orderFinder.getOrderDataResponseFromUserEvent(sourceUserId, event, pretixInformation)
                );
                break;
            }
            default: break;
        }

        return ExchangeConfirmationStatusResponse.builder()
                .sourceUser(sourceUser)
                .sourceConfirmed(status.isSourceConfirmed())
                .targetUser(targetUser)
                .targetConfirmed(status.isTargetConfirmed())
                .action(action)
                .fullOrderExchange(orderData)
                .sourceRoomExchange(sourceRoomData)
                .sourceExtraDays(sourceExtraDays)
                .targetRoomInfoHidden(targetRoomHidden)
                .targetRoomExchange(targetRoomData)
                .targetExtraDays(targetExtraDays)
                .build();
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Long exchangeId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
