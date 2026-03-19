package net.furizon.backend.feature.gallery.usecase;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.gallery.dto.response.AdminBatchApprovalResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AdminBatchListUseCase implements UseCase<AdminBatchListUseCase.Input, AdminBatchApprovalResponse> {
    @NotNull
    private final Cache<Long, List<Pair<Long, Long>>> userToLastRanges;

    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final GalleryConfig galleryConfig;

    private long firstPendingMediaId = -1L;

    public AdminBatchListUseCase(
        @NotNull final UploadFinder uploadFinder,
        @NotNull final UserFinder userFinder,
        @NotNull final GalleryConfig galleryConfig
    ) {
        this.uploadFinder = uploadFinder;
        this.userFinder = userFinder;
        this.galleryConfig = galleryConfig;

        userToLastRanges =  Caffeine.newBuilder()
                .expireAfterWrite(galleryConfig.getAdminApproval().getReservationBatchMins(), TimeUnit.MINUTES)
                .build();
    }


    @Override
    public @NotNull AdminBatchApprovalResponse executor(@NotNull AdminBatchListUseCase.Input input) {
        boolean firstRequest = input.firstRequest;
        long userId = input.user.getUserId();

        log.info("User {} is asking for a new upload approval batch (firstReq = {})",
                userId, firstRequest);

        // Try to first get results using the last user point
        List<GalleryUploadPreview> res = getResults(userId, firstRequest);
        // If we didn't had any results, try restarting the process to fill holes
        if (res.isEmpty()) {
            log.debug("getResults returned 0 results. Trying again with first request = true");
            res = getResults(userId, true);
        }

        //Then take the results and compute the new user ranges, replace the old ones
        //We assume that the list is already ordered by id
        GalleryUploadPreview firstRes = null;
        if (res.isEmpty()) {
            log.debug("getResults returned AGAIN 0 results. emptying user cache, approval is done");
            userToLastRanges.invalidate(userId);
        } else {
            List<Pair<Long, Long>> ranges = new ArrayList<>();
            firstRes = res.getFirst();
            long lastId = firstRes.getId();
            long startId = lastId;
            for (GalleryUploadPreview upload : res) {
                long currentId = upload.getId();
                if (currentId - lastId > 1L) { //if they're not contiguous
                    ranges.add(Pair.of(startId, lastId));
                    startId = lastId;
                }

                lastId = currentId;
            }
            ranges.add(Pair.of(startId, lastId));
            log.debug("Updating user {} cache with {} new reserved ranges", userId, ranges.size());
            userToLastRanges.put(userId, ranges);
        }

        GalleryEvent event = firstRes == null ? null : uploadFinder.getGalleryEvent(firstRes.getEventId(), null);
        UserDisplayData user = firstRes == null ? null : userFinder.getDisplayUser(firstRes.getPhotographerUserId(),
                                                         event == null ? null : event.getEvent());
        return new AdminBatchApprovalResponse(res, user, event, uploadFinder.countPendingUploads());
    }

    private List<GalleryUploadPreview> getResults(long userId, boolean firstRequest) {
        long userStartingFrom = getUserStartingFrom(firstRequest, userId);
        log.debug("User {} is starting from id {} (firstReq = {})", userId, userStartingFrom, firstRequest);

        List<Pair<Long, Long>> excludedRanges = new ArrayList<>();
        userToLastRanges.asMap().forEach((usrId, ranges) -> {
            for (var range : ranges) {
                long minRange = range.getLeft();
                long maxRange = range.getRight();
                if (maxRange > userStartingFrom) {
                    excludedRanges.add(Pair.of(Math.max(minRange, userStartingFrom), maxRange));
                }
            }
        });
        log.debug("Querying with {} ranges", excludedRanges.size());

        return uploadFinder.adminBatchApprovalRetrival(
                userStartingFrom,
                galleryConfig.getAdminApproval().getBatchSize(),
                excludedRanges
        );
    }

    private long getUserStartingFrom(boolean firstRequest, long userId) {
        long userStartingFrom = 0L;
        if (!firstRequest) {
            var userStatus = userToLastRanges.getIfPresent(userId);
            if (userStatus != null) {
                var max = userStatus.stream().max(Comparator.comparingLong(Pair::getRight)).orElse(null);
                if (max != null) {
                    userStartingFrom = max.getRight();
                }
            }
        }
        return Math.max(userStartingFrom, firstPendingMediaId);
    }

    @Scheduled(fixedDelay = 36L, timeUnit = TimeUnit.HOURS)
    private void updateFirstPending() {
        log.info("Updating first pending media id");
        Long res = uploadFinder.getFirstPendingUpload(firstPendingMediaId);
        firstPendingMediaId = res == null ? -1L : res;
        log.debug("firstPendingMediaId set to {}", firstPendingMediaId);
    }

    public record Input(
        boolean firstRequest,
        @NotNull FurizonUser user
    ) {}
}
