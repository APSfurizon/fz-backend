package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.order.PretixFileUploadResponse;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileAnswerUseCase implements UseCase<UploadFileAnswerUseCase.Input, Optional<String>>  {
    private final ParameterizedTypeReference<PretixFileUploadResponse> pretixFileUploadResponse =
            new ParameterizedTypeReference<>() {};

    @Qualifier("pretixHttpClient")
    private final HttpClient pretixHttpClient;

    @Transactional
    @NotNull
    @Override
    public Optional<String> executor(@NotNull UploadFileAnswerUseCase.Input file) {
        final var request = HttpRequest.<PretixFileUploadResponse>create()
                .method(HttpMethod.POST)
                .path("/upload")
                .body(file.data, file.mediaType, file.contentSize)
                .responseParameterizedType(pretixFileUploadResponse)
                .build();

        try {
            PretixFileUploadResponse r = pretixHttpClient.send(PretixConfig.class, request).getBody();
            if (r != null) {
                return Optional.of(r.getId());
            }
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
        return Optional.empty();
    }
    public record Input(@NotNull byte[] data, @NotNull MediaType mediaType, long contentSize) {}
}
