package net.furizon.backend.feature.pretix.objects.states.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.feature.pretix.objects.states.dto.GitCountry;
import net.furizon.backend.feature.pretix.objects.states.dto.PretixStateResponse;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedList;
import java.util.List;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixStateFinder implements PretixStateFinder {
    private static final String ITALIAN_STATES_BASE_URL = "https://gist.githubusercontent.com";
    private static final String ITALIAN_STATES_PATH =
            "/LucaRosaldi/3081928/raw/elenco_province_italiane_json_array.json";
    private static final String COUNTRIES_BASE_URL = "https://raw.githubusercontent.com";
    private static final String COUNTRIES_PATH =
            "/lukes/ISO-3166-Countries-with-Regional-Codes/refs/heads/master/slim-2/slim-2.json";


    private final ParameterizedTypeReference<PretixStateResponse> pretixStatesType =
            new ParameterizedTypeReference<>() {};
    private final ParameterizedTypeReference<List<PretixState>> italyStatesType =
            new ParameterizedTypeReference<>() {};
    private final ParameterizedTypeReference<List<GitCountry>> countriesType =
            new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Override
    public @NotNull List<PretixState> getPretixStates(@NotNull String countryCode) {
        log.info("Loading state list from pretix for country {}", countryCode);
        final var request = HttpRequest.<PretixStateResponse>create()
                .method(HttpMethod.GET)
                .overrideBasePath("")
                .path("/js_helpers/states/")
                .queryParam("country", countryCode)
                .responseParameterizedType(pretixStatesType)
                .build();
        try {
            var res = pretixHttpClient.send(PretixConfig.class, request).getBody();
            return res != null ? res.getData() : new LinkedList<>();
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }

    @Override
    public @NotNull List<PretixState> getItalianStates() {
        log.info("Loading state list for italy");
        final var request = HttpRequest.<List<PretixState>>create()
                .method(HttpMethod.GET)
                .overrideBaseUrl(ITALIAN_STATES_BASE_URL)
                .overrideBasePath("")
                .path(ITALIAN_STATES_PATH)
                .responseParameterizedType(italyStatesType)
                .build();
        try {
            var res = pretixHttpClient.send(PretixConfig.class, request).getBody();
            return res != null ? res : new LinkedList<>();
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }

    @Override
    public @NotNull List<PretixState> getCountries() {
        log.info("Loading country list");
        final var request = HttpRequest.<List<GitCountry>>create()
                .method(HttpMethod.GET)
                .overrideBaseUrl(COUNTRIES_BASE_URL)
                .overrideBasePath("")
                .path(COUNTRIES_PATH)
                .responseParameterizedType(countriesType)
                .build();
        try {
            var res = pretixHttpClient.send(PretixConfig.class, request).getBody();
            return res != null ? res.stream().map(GitCountry::toState).toList() : new LinkedList<>();
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
