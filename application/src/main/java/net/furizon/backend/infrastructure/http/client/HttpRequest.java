package net.furizon.backend.infrastructure.http.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
    private final MultiValueMap<String, String> headers;

    @NotNull
    private final Map<String, ?> uriVariables;

    @NotNull
    private final MultiValueMap<String, String> queryParams;

    @Nullable
    private final Object body;

    @Nullable
    private final Class<R> responseType;

    @Nullable
    private final ParameterizedTypeReference<R> responseParameterizedType;

    public static class Builder<R> {
        private HttpMethod method = null;
        private String path = null;
        private Object body = null;
        private Class<R> responseType;
        private ParameterizedTypeReference<R> responseParameterizedType;

        private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        private final Map<String, String> uriVariables = new LinkedHashMap<>();
        private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

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

        public Builder<R> headers(@NotNull final MultiValueMap<String, String> headers) {
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

        public Builder<R> queryParams(@NotNull final MultiValueMap<String, String> queryParams) {
            this.queryParams.addAll(queryParams);
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
            @NotNull final ParameterizedTypeReference<R> responseParameterizedType
        ) {
            this.responseParameterizedType = responseParameterizedType;
            return this;
        }

        public HttpRequest<R> build() {
            return new HttpRequest<R>(
                method,
                path,
                headers,
                uriVariables,
                queryParams,
                body,
                responseType,
                responseParameterizedType
            );
        }
    }

    public static <R> HttpRequest.Builder<R> create() {
        return new Builder<>();
    }
}
