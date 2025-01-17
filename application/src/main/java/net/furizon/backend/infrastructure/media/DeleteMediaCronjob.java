package net.furizon.backend.infrastructure.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.media.usecase.RemoveDanglingMediaUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteMediaCronjob {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;

    @Scheduled(cron = "${storage.delete-dangling-media-cronjob}")
    public void deleteDanglingMedia() {
        log.info("[DANGLING MEDIA] Deleting dangling media cronjob is running");
        long deleted = useCaseExecutor.execute(RemoveDanglingMediaUseCase.class, 0); //input is useless
        log.info("[DANGLING MEDIA] Deleting dangling media cronjob is completed: Deleted {} objects", deleted);
    }
}
