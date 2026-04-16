package net.furizon.backend.feature.pretix.objects.checkins.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.PagedPretixCheckinHistory;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinHistoryOrder;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;
import java.util.Objects;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestCheckinHistoryFinder implements PretixCheckinHistoryFinder {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @NotNull PagedPretixCheckinHistory getPagedCheckinLists(
            @NotNull String organizer,
            @NotNull String event,

            @Nullable OffsetDateTime createdSince,
            @Nullable OffsetDateTime createdBefore,
            @Nullable OffsetDateTime datetimeSince,
            @Nullable OffsetDateTime datetimeBefore,
            @Nullable Boolean successful,
            @Nullable Long checkinListId,
            @Nullable CheckinType type,
            @Nullable Boolean autoCheckedIn,
            @Nullable CheckinHistoryOrder order,

            @Nullable Integer page) {
        final var request = HttpRequest.<PagedPretixCheckinHistory>create()
                .method(HttpMethod.GET)
                .path("/organizers/{organizer}/events/{event}/checkins/")
                .uriVariable("organizer", organizer)
                .uriVariable("event", event)
                .responseType(PagedPretixCheckinHistory.class);

        if (page != null) {
            request.queryParam("page", String.valueOf(page));
        }

        if (createdSince != null) {
            request.queryParam("created_since", createdSince.toString());
        }
        if (createdBefore != null) {
            request.queryParam("created_before", createdBefore.toString());
        }
        if (datetimeSince != null) {
            request.queryParam("datetime_since", datetimeSince.toString());
        }
        if (datetimeBefore != null) {
            request.queryParam("datetime_before", datetimeBefore.toString());
        }
        if (successful != null) {
            request.queryParam("successful", String.valueOf(successful));
        }
        if (checkinListId != null) {
            request.queryParam("list", String.valueOf(checkinListId));
        }
        if (type != null) {
            request.queryParam("type", type.toString().toLowerCase());
        }
        if (autoCheckedIn != null) {
            request.queryParam("auto_checked_in", String.valueOf(autoCheckedIn));
        }
        if (order != null) {
            request.queryParam("order", order.getPretixField());
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
