package net.furizon.backend.feature.room.action.exchangeRoom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ExchangeRoomRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestExchangeRoom implements ExchangeRoomAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;
    @NotNull
    private final PretixConfig pretixConfig;

    @Override
    public boolean invoke(
            @NotNull String sourceOrderCode,
            long sourceRoomPositionId,
            @Nullable Long sourceEarlyPositionId,
            @Nullable Long sourceLatePositionId,

            @NotNull String destOrderCode,
            long destRoomPositionId,
            @Nullable Long destEarlyPositionId,
            @Nullable Long destLatePositionId,

            @Nullable String manualPaymentComment,
            @Nullable String manualRefundComment,
            @NotNull Event event) {
        log.info("Exchanging room using pretix plugin. "
               + "srcOrderCode={}, srcRoomPositionId={}, srcEarlyPositionId={}, srcLatePositionId={};  "
               + "dstOrderCode={}, dstRoomPositionId={}, dstEarlyPositionId={}, dstLatePositionId={}",
                sourceOrderCode, sourceRoomPositionId, sourceEarlyPositionId, sourceLatePositionId,
                destOrderCode, destRoomPositionId, destEarlyPositionId, destLatePositionId);
        final var pair = event.getOrganizerAndEventPair();
        final var request = HttpRequest.<String>create()
                .method(HttpMethod.POST)
                .overrideBasePath(pretixConfig.getShop().getBasePath())
                .path("/{organizer}/{event}/fzbackendutils/api/exchange-rooms/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ExchangeRoomRequest.builder()
                        .sourceOrderCode(sourceOrderCode)
                        .sourceRoomPositionId(sourceRoomPositionId)
                        .sourceEarlyPositionId(sourceEarlyPositionId)
                        .sourceLatePositionId(sourceLatePositionId)

                        .destOrderCode(destOrderCode)
                        .destRoomPositionId(destRoomPositionId)
                        .destEarlyPositionId(destEarlyPositionId)
                        .destLatePositionId(destLatePositionId)

                        .manualPaymentComment(manualPaymentComment)
                        .manualRefundComment(manualRefundComment)
                    .build()
                )
                .responseType(String.class)
                .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getResponseEntity().getStatusCode().is2xxSuccessful();
        } catch (final HttpClientErrorException ex) {
            log.error("Error updating bundle status", ex);
            return false;
        }
    }
}
