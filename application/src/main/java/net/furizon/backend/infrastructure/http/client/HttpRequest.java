package net.furizon.backend.infrastructure.http.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class HttpRequest<R> {
    @NotNull
    private final HttpMethod method;

    @NotNull
    private final String path;

    @NotNull
    private final HttpHeaders headers;

    @NotNull
    private final Map<String, ?> uriVariables;

    @NotNull
    private final MultiValueMap<@NotNull String, @NotNull String> queryParams;

    @Nullable
    private final Object body;

    @Nullable
    private final MediaType contentType;

    @Nullable
    private final Class<R> responseType;

    @Nullable
    private final ParameterizedTypeReference<@NotNull R> responseParameterizedType;

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String overrideBaseUrl;
    @NotNull public String overrideBaseUrl() {
        return overrideBaseUrl != null ? overrideBaseUrl : "";
    }
    public boolean shouldOverrideUrl() {
        return overrideBaseUrl != null;
    }

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String overrideBasePath;
    @NotNull public String overrideBasePath() {
        return overrideBasePath != null ? overrideBasePath : "";
    }
    public boolean shouldOverridePath() {
        return overrideBasePath != null;
    }

    //Overriding the getter for naming reasons
    @Getter(AccessLevel.NONE)
    private final boolean sendConfigHeaders;
    public final boolean sendConfigHeaders() {
        return sendConfigHeaders;
    }

    public static class Builder<R> {
        private Class<R> responseType;
        private String path = null;
        private Object body = null;
        private HttpMethod method = null;
        private MediaType contentType = null;
        private String overrideBaseUrl = null;
        private String overrideBasePath = null;
        private boolean sendConfigHeaders = true;
        private ParameterizedTypeReference<@NotNull R> responseParameterizedType;

        private final HttpHeaders headers = new HttpHeaders();
        private final Map<String, String> uriVariables = new LinkedHashMap<>();
        private final MultiValueMap<@NotNull String, @NotNull String> queryParams = new LinkedMultiValueMap<>();

        public Builder<R> sendConfigHeaders(boolean sendConfigHeaders) {
            this.sendConfigHeaders = sendConfigHeaders;
            return this;
        }

        public Builder<R> overrideBasePath(@NotNull String overrideBasePath) {
            this.overrideBasePath = overrideBasePath;
            return this;
        }

        public Builder<R> overrideBaseUrl(@NotNull String overrideBaseUrl) {
            this.overrideBaseUrl = overrideBaseUrl;
            return this;
        }

        public Builder<R> method(@NotNull final HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder<R> path(@NotNull final String path) {
            this.path = path;
            return this;
        }

        public Builder<R> header(@NotNull final String key, @NotNull final String value) {
            this.headers.add(key, value);
            return this;
        }

        public Builder<R> headers(@NotNull final MultiValueMap<@NotNull String, @NotNull String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder<R> headers(@NotNull final HttpHeaders headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder<R> uriVariable(@NotNull final String key, @NotNull final String value) {
            this.uriVariables.put(key, value);
            return this;
        }

        public Builder<R> queryParam(@NotNull final String key, @NotNull final String value) {
            this.queryParams.add(key, value);
            return this;
        }

        public Builder<R> queryParams(@NotNull final MultiValueMap<@NotNull String, @NotNull String> queryParams) {
            this.queryParams.addAll(queryParams);
            return this;
        }

        public Builder<R> contentType(@NotNull final MediaType mediaType) {
            this.contentType = mediaType;
            return this;
        }

        public Builder<R> body(@NotNull final Object body) {
            this.body = body;
            return this;
        }

        public Builder<R> responseType(@NotNull final Class<R> responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder<R> responseParameterizedType(
            @NotNull final ParameterizedTypeReference<@NotNull R> responseParameterizedType
        ) {
            this.responseParameterizedType = responseParameterizedType;
            return this;
        }

        public HttpRequest<R> build() {
            return new HttpRequest<>(
                method,
                path,
                headers,
                uriVariables,
                queryParams,
                body,
                contentType,
                responseType,
                responseParameterizedType,
                overrideBaseUrl,
                overrideBasePath,
                sendConfigHeaders
            );
        }
    }

    public static <R> HttpRequest.Builder<R> create() {
        return new Builder<>();
    }
}
