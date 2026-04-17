package net.furizon.backend.feature.pretix.objects.checkins.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinSearchOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
class RestCheckinSearchFinder implements PretixCheckinSearchFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixPosition>> checkins =
            new ParameterizedTypeReference<>() {};
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @NotNull PretixPaging<PretixPosition> getPagedCheckinSearchResults(
            @NotNull String organizer,

            @Nullable String search,
            @Nullable Long checkinListId,
            @Nullable Boolean hasCheckedIn,
            @Nullable CheckinSearchOrder order,

            @Nullable Integer page) {
        final var request = HttpRequest.<PretixPaging<PretixPosition>>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/checkinrpc/search/")
                .uriVariable("organizer", organizer)
                .responseParameterizedType(checkins);

        if (page != null) {
            request.queryParam("page", String.valueOf(page));
        }

        if (search != null) {
            request.queryParam("search", search);
        }
        if (checkinListId != null) {
            request.queryParam("list", String.valueOf(checkinListId));
        }
        if (hasCheckedIn != null) {
            request.queryParam("has_checkin", String.valueOf(hasCheckedIn));
        }
        if (order != null) {
            request.queryParam("ordering ", order.getPretixField());
        }

        try {
            return Objects.requireNonNull(pretixHttpClient.send(PretixConfig.class, request.build()).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        } catch (final NullPointerException ex) {
            log.error("Null response body");
            throw ex;
        }
    }
}
