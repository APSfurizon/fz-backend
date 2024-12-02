package net.furizon.backend.feature.pretix.objects.question.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.Const.PRETIX_HTTP_CLIENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestPretixQuestionFinder implements PretixQuestionFinder {
    private final ParameterizedTypeReference<PretixPaging<PretixQuestion>> pretixPagedQuestion =
        new ParameterizedTypeReference<>() {};

    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;

    @NotNull
    @Override
    public PretixPaging<PretixQuestion> getPagedQuestions(
        @NotNull String organizer,
        @NotNull String event,
        int page
    ) {
        final var request = HttpRequest.<PretixPaging<PretixQuestion>>create()
            .method(HttpMethod.GET)
            .path("/organizers/{organizer}/events/{event}/questions/")
            .queryParam("page", String.valueOf(page))
            .uriVariable("organizer", organizer)
            .uriVariable("event", event)
            .responseParameterizedType(pretixPagedQuestion)
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
