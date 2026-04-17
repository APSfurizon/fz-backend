package net.furizon.backend.feature.gallery.usecase.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.processor.handleJob.GalleryProcessorHandleJobAction;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJobStatus;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletedWebhookUseCase implements UseCase<GalleryProcessorJob, Boolean> {
    @NotNull GalleryProcessorHandleJobAction galleryProcessorHandleJobAction;

    @Override
    public @NotNull Boolean executor(@NotNull GalleryProcessorJob job) {
        log.info("Gallery Processor: Receiving job {} completition done via webhook", job.getId());
        if (job.getStatus() != GalleryProcessorJobStatus.DONE) {
            log.warn("Job {} received via webhook is in invalid state {}", job.getId(), job.getStatus());
            return false;
        }
        return galleryProcessorHandleJobAction.invoke(job);
    }
}
