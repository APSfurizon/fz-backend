package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.dto.PretixFileUploadResponse;
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

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

// TODO -> Why do we need this class if we don't use it?
@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileAnswerUseCase implements UseCase<UploadFileAnswerUseCase.Input, Optional<String>> {
    private final ParameterizedTypeReference<PretixFileUploadResponse> pretixFileUploadResponse =
        new ParameterizedTypeReference<>() {
        };

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @Transactional
    @NotNull
    @Override
    public Optional<String> executor(@NotNull UploadFileAnswerUseCase.Input file) {
        // TODO -> Action
        final var request = HttpRequest.<PretixFileUploadResponse>create()
            .method(HttpMethod.POST)
            .path("/upload")
            .body(file.data)
            .responseParameterizedType(pretixFileUploadResponse)
            .build();

        try {
            PretixFileUploadResponse r = pretixHttpClient.send(PretixConfig.class, request).getBody();
            return r != null
                ? Optional.of(r.getId())
                : Optional.empty();
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }

    public record Input(
        byte @NotNull [] data,
        @NotNull MediaType mediaType,
        long contentSize
    ) {}
}
