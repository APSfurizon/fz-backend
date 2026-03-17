package net.furizon.backend.feature.gallery.usecase.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.processor.handleJob.GalleryProcessorHandleJobAction;
import net.furizon.backend.feature.gallery.action.processor.retryJob.GalleryProcessorRetryJobAction;
import net.furizon.backend.feature.gallery.action.processor.submitJob.GalleryProcessorSubmitJobAction;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.gallery.finder.processor.GalleryProcessorJobFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollGalleryProcessorUseCase implements UseCase<Integer, Integer> {
    @NotNull private final GalleryProcessorHandleJobAction galleryProcessorHandleJobAction;
    @NotNull private final GalleryProcessorSubmitJobAction galleryProcessorSubmitJobAction;
    @NotNull private final GalleryProcessorRetryJobAction galleryProcessorRetryJobAction;

    @NotNull private final GalleryProcessorJobFinder galleryProcessorJobFinder;
    @NotNull private final UploadFinder uploadFinder;

    @Override
    @NotNull
    public Integer executor(@NotNull Integer input) {
        int pollNo = 0;


        List<Long> unprocessed = uploadFinder.getUnprocessedUploadIds();
        List<Long> toSubmit = new ArrayList<>((unprocessed.size() / 2) + 1);
        List<Long> toRetry = new ArrayList<>(toSubmit.size());
        for (long uploadId : unprocessed) {
            try {
                pollNo++;
                var j = galleryProcessorJobFinder.getJobByReqId(uploadId);
                if (j.isEmpty()) {
                    log.warn("[GALLERY PROCESSOR] getJobByReqId returned null for {}", uploadId);
                    toSubmit.add(uploadId);
                    continue;
                }

                GalleryProcessorJob job = j.get();
                switch (job.getStatus()) {
                    case NOT_FOUND -> {
                        log.warn("[GALLERY PROCESSOR] No gallery processor job found for upload id {}", uploadId);
                        toSubmit.add(uploadId);
                    }
                    case FAILED -> {
                        log.warn("[GALLERY PROCESSOR] Job {} has failed. Retrying", uploadId);
                        toRetry.add(uploadId);
                    }
                    case DONE -> {
                        log.info("[GALLERY PROCESSOR] Job {} completed!", uploadId);
                        try {
                            galleryProcessorHandleJobAction.invoke(job);
                        } catch (Exception e) {
                            log.error("[GALLERY PROCESSOR] Gallery processor job execution failed for job {}", uploadId, e);
                        }
                    }
                    default -> {
                    }
                }
            } catch (Exception e) {
                log.warn("[GALLERY PROCESSOR] exception while requesting job {}", uploadId, e);
            }
        }

        for (long uploadId : toSubmit) {
            String fileName = uploadFinder.getMainMediaFilenameFromUploadId(uploadId);
            if (fileName == null) {
                log.warn("[GALLERY PROCESSOR] Unable to fetch filename from db for upload {}", uploadId);
                continue;
            }
            galleryProcessorSubmitJobAction.invoke(uploadId, fileName);
        }

        for (long uploadId : toRetry) {
            galleryProcessorRetryJobAction.invoke(uploadId);
        }

        return pollNo;
    }
}
