package net.furizon.backend.feature.gallery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.usecase.processor.PollGalleryProcessorUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryProcessorPollingCronjob {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;

    @Scheduled(fixedRateString = "${gallery.job-processor.poll-interval}", timeUnit = TimeUnit.MINUTES)
    public void deleteExpiredUploadProgress() {
        int polled = useCaseExecutor.execute(PollGalleryProcessorUseCase.class, 0); //input is useless
        if (polled > 0) {
            log.info("[GALLERY PROCESSOR] Polled {} elements", polled);
        }
    }
}
