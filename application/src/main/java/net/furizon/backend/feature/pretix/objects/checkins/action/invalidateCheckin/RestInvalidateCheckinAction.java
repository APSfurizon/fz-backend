package net.furizon.backend.feature.pretix.objects.checkins.action.invalidateCheckin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.PretixCancelCheckinRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestInvalidateCheckinAction implements PretixInvalidateCheckinAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public boolean invoke(@NotNull String organizer,
                          @NotNull String nonce,
                          @NotNull List<Long> checkinListIds,
                          @Nullable String explanation) {
        log.info("Canceling checkin for lists {} with nonce {}", checkinListIds, nonce);
        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .path("/organizers/{organizer}/checkinrpc/annul/")
                .uriVariable("organizer", organizer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PretixCancelCheckinRequest(nonce, checkinListIds, explanation))
                .responseType(Void.class)
                .build();

        try {
            var resp = pretixHttpClient.send(PretixConfig.class, request, ByteBuffer.class);
            if (resp.isError()) {
                throw new ApiException(
                        HttpStatus.valueOf(resp.getErrorResponse().getStatusCode().value()),
                        new String(resp.getErrorEntity().array())
                );
            }
            return true;
        } catch (final HttpClientErrorException | IOException ex) {
            log.error("Error canceling a checkin", ex);
            return false;
        }
    }
}
