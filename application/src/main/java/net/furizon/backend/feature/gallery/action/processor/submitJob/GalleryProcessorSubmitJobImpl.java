package net.furizon.backend.feature.gallery.action.processor.submitJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJobSubmit;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static net.furizon.backend.feature.gallery.GalleryProcessorHttpClient.GALLERY_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class GalleryProcessorSubmitJobImpl implements GalleryProcessorSubmitJobAction {
    @Qualifier(GALLERY_HTTP_CLIENT)
    private final HttpClient galleryHttpClient;

    @Override
    @NotNull
    public Optional<GalleryProcessorJob> invoke(long reqId, @NotNull String fileName) {
        log.info("Submitting job {} with key {}", reqId, fileName);
        final var request = HttpRequest.<GalleryProcessorJob>create()
                .method(HttpMethod.POST)
                .path("/job/")
                .body(new GalleryProcessorJobSubmit(reqId, fileName))
                .responseType(GalleryProcessorJob.class)
                .build();

        try {
            return Optional
                    .ofNullable(galleryHttpClient.send(GalleryConfig.class, request).getBody());
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
