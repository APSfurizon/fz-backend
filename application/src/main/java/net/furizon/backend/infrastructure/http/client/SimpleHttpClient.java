package net.furizon.backend.infrastructure.http.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleHttpClient implements HttpClient {
    @NotNull
    private final RestClient restClient;

    @NotNull
    private final Map<Class<? extends HttpConfig>, HttpConfig> httpConfigsMap;

    public SimpleHttpClient(
        @NotNull final RestClient restClient,
        @NotNull final List<HttpConfig> httpConfigs
    ) {
        this.restClient = restClient;
        httpConfigsMap = httpConfigs
            .stream()
            .collect(
                Collectors.toMap(
                    HttpConfig::getClass,
                    Function.identity()
                )
            );
    }

    @NotNull
    @Override
    public <C extends HttpConfig, R> ResponseEntity<R> send(
        @NotNull final Class<C> configClass,
        @NotNull final HttpRequest<R> request
    ) {
        final HttpConfig config = httpConfigsMap.get(configClass);
        if (config == null) {
            throw new IllegalArgumentException("No config found for " + configClass.getSimpleName());
        }

        String baseUrl = request.shouldOverrideUrl() ? request.overrideBaseUrl() : config.getBaseUrl();
        String basePath = request.shouldOverridePath() ? request.overrideBasePath() : config.getBasePath();

        UriBuilder builder = new DefaultUriBuilderFactory(baseUrl)
            .builder()
            .path(basePath + request.getPath());

        if (!request.getQueryParams().isEmpty()) {
            builder = builder.queryParams(request.getQueryParams());
        }


        RestClient.RequestBodySpec requestBodySpec = restClient
            .method(request.getMethod())
            .uri(builder.build(request.getUriVariables()))
            .headers((headers) -> {
                if(request.sendConfigHeaders()) {
                    headers.addAll(config.headers());
                }
                if (!request.getHeaders().isEmpty()) {
                    headers.addAll(request.getHeaders());
                }
            });

        if (request.getBody() != null) {
            requestBodySpec = requestBodySpec.body(request.getBody());
        }
        if (request.getContentType() != null) {
            requestBodySpec = requestBodySpec.contentType(request.getContentType());
        }

        if (request.getResponseParameterizedType() == null && request.getResponseType() == null) {
            throw new IllegalArgumentException("responseParameterizedType or responseType is required");
        }

        if (request.getResponseParameterizedType() != null) {
            return requestBodySpec
                .retrieve()
                .toEntity(request.getResponseParameterizedType());
        } else {
            return requestBodySpec
                .retrieve()
                .toEntity(request.getResponseType());
        }
    }
}
