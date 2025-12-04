package net.furizon.backend.feature.room.action.transferOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.room.dto.request.TransferOrderRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.http.client.dto.GenericErrorResponse;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.fzBackendUtils.FzBackendUtilsErrorCodes;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestTransferOrder implements TransferPretixOrderAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;
    @NotNull
    private final PretixConfig pretixConfig;
    @NotNull
    private final TranslationService translationService;

    @Override
    public boolean invoke(
        @NotNull String orderCode,
        long positionId,
        long questionId,
        long newUserId,

        @Nullable String paymentComment,
        @Nullable String refundComment,

        @NotNull Event event
    ) {
        log.info("Transferring order using pretix plugin. "
               + "orderCode={}, positionId={}, questionId={}, newUserId={}",
                orderCode, positionId, questionId, newUserId);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<Void>create()
            .method(HttpMethod.POST)
            .overrideBasePath(pretixConfig.getShop().getBasePath())
            .path("/{organizer}/{event}/fzbackendutils/api/transfer-order/")
            .uriVariable("organizer", pair.getOrganizer())
            .uriVariable("event", pair.getEvent())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                TransferOrderRequest.builder()
                    .orderCode(orderCode)
                    .positionId(positionId)
                    .questionId(questionId)
                    .newUserId(newUserId)

                    .manualPaymentComment(paymentComment)
                    .manualRefundComment(refundComment)
                .build()
            )
            .responseType(Void.class)
            .build();
        try {
            var response = pretixHttpClient.send(PretixConfig.class, request, GenericErrorResponse.class);
            if (response.isError()) {
                GenericErrorResponse err = response.getErrorEntity();
                String message = err.getError();
                int code = response.getErrorResponse().getStatusCode().value();
                return switch (code) {
                    case FzBackendUtilsErrorCodes.STATUS_CODE_PAYMENT_STATUS_INVALID -> throw new ApiException(
                            translationService.error("pretix.orders_flow.payment_illegal_state", message),
                            OrderWorkflowErrorCode.PAYMENT_INVALID_STATE
                    );
                    case FzBackendUtilsErrorCodes.STATUS_CODE_REFUND_STATUS_INVALID -> throw new ApiException(
                            translationService.error("pretix.orders_flow.refund_illegal_state", message),
                            OrderWorkflowErrorCode.REFUND_INVALID_STATE
                    );
                    default -> false;
                };
            } else {
                return response.getResponseEntity().getStatusCode().is2xxSuccessful();
            }
        } catch (final HttpClientErrorException | IOException ex) {
            log.error("Error updating bundle status", ex);
            return false;
        }
    }
}
