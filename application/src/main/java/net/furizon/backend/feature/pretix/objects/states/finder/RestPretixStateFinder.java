package net.furizon.backend.feature.pretix.objects.states.finder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.feature.pretix.objects.states.dto.GitPhoneCountry;
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
    private static final String COUNTRIES_BASE_URL = "https://gist.githubusercontent.com";
    private static final String COUNTRIES_PATH =
            "/anubhavshrimal/75f6183458db8c453306f93521e93d37/raw/CountryCodes.json";


    private final ParameterizedTypeReference<PretixStateResponse> pretixStatesType =
            new ParameterizedTypeReference<>() {};
    private final ParameterizedTypeReference<String> italyStatesType =
            new ParameterizedTypeReference<>() {};
    private final ParameterizedTypeReference<String> countriesType =
            new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    private final ObjectMapper objectMapper;

    @Override
    public @NotNull List<PretixState> getPretixStates(@NotNull String countryCode) {
        log.info("Loading state list from pretix for country {}", countryCode);
        final var request = HttpRequest.<PretixStateResponse>create()
                .method(HttpMethod.GET)
                .overrideBasePath("")
                .path("/js_helpers/states/")
                .queryParam("country", countryCode)
                .responseParameterizedType(pretixStatesType)
                .sendConfigHeaders(false)
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
        final var request = HttpRequest.<String>create()
                .method(HttpMethod.GET)
                .overrideBaseUrl(ITALIAN_STATES_BASE_URL)
                .overrideBasePath("")
                .path(ITALIAN_STATES_PATH)
                .responseParameterizedType(italyStatesType)
                .sendConfigHeaders(false)
                .build();
        try {
            String res = pretixHttpClient.send(PretixConfig.class, request).getBody();
            //Unfortunately, the response content type header is set to text/plain, and the RestClient
            //Won't interpret it as a json. This is a workaround
            if (res != null) {
                return objectMapper.readValue(
                        res,
                        TypeFactory.defaultInstance().constructCollectionType(List.class, PretixState.class)
                );
            } else {
                log.error("Unable to fetch italian states. Returning an empty list");
                return new LinkedList<>();
            }
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        } catch (final JsonProcessingException ex) {
            log.error("Unable to decode italian states json");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull List<PretixState> getCountries() {
        log.info("Loading country list");
        final var request = HttpRequest.<String>create()
                .method(HttpMethod.GET)
                .overrideBaseUrl(COUNTRIES_BASE_URL)
                .overrideBasePath("")
                .path(COUNTRIES_PATH)
                .responseParameterizedType(countriesType)
                .sendConfigHeaders(false)
                .build();
        try {
            String res = pretixHttpClient.send(PretixConfig.class, request).getBody();
            //Unfortunately, the response content type header is set to text/plain, and the RestClient
            //Won't interpret it as a json. This is a workaround
            if (res != null) {
                List<GitPhoneCountry> countries = objectMapper.readValue(
                        res,
                        TypeFactory.defaultInstance().constructCollectionType(List.class, GitPhoneCountry.class)
                );
                return countries.stream().map(k -> (PretixState) k.toPhoneCountry()).toList();
            } else {
                log.error("Unable to fetch countries. Returning an empty list");
                return new LinkedList<>();
            }
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        } catch (final JsonProcessingException ex) {
            log.error("Unable to decode countries json");
            throw new RuntimeException(ex);
        }
    }
}
