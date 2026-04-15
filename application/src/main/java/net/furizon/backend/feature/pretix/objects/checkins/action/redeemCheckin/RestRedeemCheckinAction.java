package net.furizon.backend.feature.pretix.objects.checkins.action.redeemCheckin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.RedeemCheckinRequest;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.RedeemCheckinResponse;
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

import java.util.List;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestRedeemCheckinAction implements PretixRedeemCheckinAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @Nullable RedeemCheckinResponse invoke(@NotNull String secret,
                                                  @NotNull String organizer,
                                                  @NotNull String nonce,
                                                  @NotNull List<Long> checkinListIds) {
        log.info("Checking in secret {} for lists {} with nonce {}", secret, checkinListIds, nonce);
        final var request = HttpRequest.<RedeemCheckinResponse>create()
                .method(HttpMethod.POST)
                .path("/api/v1/organizers/{organizer}/checkinrpc/redeem/")
                .uriVariable("organizer", organizer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemCheckinRequest(secret, checkinListIds, nonce))
                .responseType(RedeemCheckinResponse.class)
                .build();

        try {
            var resp = pretixHttpClient.send(PretixConfig.class, request);
            return resp.getBody();
        } catch (final HttpClientErrorException ex) {
            log.error("Error doing a checkin", ex);
            return null;
        }
    }
}
