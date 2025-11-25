package net.furizon.backend.infrastructure.http.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HttpClient {
    /**
     * @throws org.springframework.web.client.HttpClientErrorException if the response status code is not 2xx
     * @throws org.springframework.web.client.RestClientException      if there is an error while executing the request
     */
    @NotNull
    <C extends HttpConfig, R, E> HttpResponse<R, E> send(
            @NotNull final Class<C> configClass,
            @NotNull final HttpRequest<R> request
    );

    /**
     * @throws org.springframework.web.client.HttpClientErrorException if the response status code is not 2xx
     * @throws org.springframework.web.client.RestClientException      if there is an error while executing the request
     */
    @NotNull
    <C extends HttpConfig, R, E> HttpResponse<R, E> send(
        @NotNull final Class<C> configClass,
        @NotNull final HttpRequest<R> request,
        @Nullable final Class<E> errorClass
    );
}
