package net.furizon.backend.infrastructure.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.furizon.backend.infrastructure.http.client.dto.GenericErrorResponse;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.io.InputStream;
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
    public <C extends HttpConfig, R, E> HttpResponse<R, E> send(
            @NotNull final Class<C> configClass,
            @NotNull final HttpRequest<R> request) {
        return send(configClass, request, null);
    }

    @NotNull
    @Override
    public <C extends HttpConfig, R, E> HttpResponse<R, E> send(
            @NotNull final Class<C> configClass,
            @NotNull final HttpRequest<R> request,
            @Nullable final Class<E> errorClass
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
                if (request.sendConfigHeaders()) {
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

        var v = requestBodySpec.retrieve();

        MutableObject<E> errObj = new MutableObject<>();
        MutableObject<ClientHttpResponse> errorResponse = new MutableObject<>();
        if (errorClass != null) {
            v = v.onStatus(x -> !x.is2xxSuccessful(), (req, resp) -> {
                InputStream body = resp.getBody();
                ObjectMapper mapper = new ObjectMapper();
                errObj.setValue(mapper.readValue(body, errorClass));
                errorResponse.setValue(resp);
            });
        }

        ResponseEntity<R> respObj = null;
        if (request.getResponseParameterizedType() != null) {
            respObj = v.toEntity(request.getResponseParameterizedType());
        } else {
            respObj = v.toEntity(request.getResponseType());
        }

        return new HttpResponse<R, E>(respObj, errorResponse.get(), errObj.get());
    }
}
