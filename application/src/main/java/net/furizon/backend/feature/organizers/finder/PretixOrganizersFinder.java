package net.furizon.backend.feature.organizers.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.organizers.Organizer;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PretixOrganizersFinder implements OrganizersFinder {
    @Qualifier("pretixHttpClient")
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<Organizer> getPagedOrganizers(int page) {
        final var request = HttpRequest.<PretixPaging<Organizer>>create()
            .method(HttpMethod.GET)
            .path("/organizers")
            .queryParam("page", String.valueOf(page))
            .responseParameterizedType(PretixPaging.parameterizedType())
            .build();

        try {
            return Optional
                .ofNullable(pretixHttpClient.send(PretixConfig.class, request).getBody())
                .orElse(PretixPaging.empty());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
