package net.furizon.backend.feature.gallery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.usecase.uploadProgress.DeleteExpiredUploadProgressUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiredUploadProgressRemovalCronjob {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;

    @Scheduled(fixedRateString = "${gallery.delete-upload-progress-interval}", timeUnit = TimeUnit.MINUTES)
    public void deleteExpiredUploadProgress() {
        int deleted = useCaseExecutor.execute(DeleteExpiredUploadProgressUseCase.class, 0); //input is useless
        if (deleted > 0) {
            log.info("[EXPIRED UPLOAD PROGRESS] Deleting expired upload progress completed: Deleted {} objects",
                    deleted);
        }
    }
}
