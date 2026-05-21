package net.furizon.backend.infrastructure.media.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.gallery.finder.UploadProgressFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.action.PhysicallyDeleteMediaAction;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import net.furizon.backend.infrastructure.s3.actions.listObjects.S3ListObjects;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDanglingMediaUseCase implements UseCase<Integer, Long> {
    @NotNull private final DeleteMediaAction deleteMediaAction;
    @NotNull private final PhysicallyDeleteMediaAction physicallyDeleteMediaAction;
    @NotNull private final UploadProgressFinder uploadProgressFinder;
    @NotNull private final UploadFinder uploadFinder;
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final StorageConfig storageConfig;

    @NotNull private final S3ListObjects s3ListObjects;
    @NotNull private final S3DeleteUpload s3DeleteUpload;

    private static final ReentrantLock MEDIA_WRITE_MUTEX = new ReentrantLock(true);

    public static void mediaWriteMutexLockException() {
        boolean lock = mediaWriteMutexTryLock();
        if (!lock) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    TranslationService.getInstance().error("common.cannot_upload_rn"),
                    GeneralResponseCodes.CANNOT_UPLOAD_MOMENTARLY
            );
        }
    }
    public static boolean mediaWriteMutexTryLock() {
        return MEDIA_WRITE_MUTEX.tryLock();
    }
    public static void mediaWriteMutexLock() {
        MEDIA_WRITE_MUTEX.lock();
    }
    public static void mediaWriteMutexUnlock() {
        MEDIA_WRITE_MUTEX.unlock();
    }

    @Override
    public @NotNull Long executor(@NotNull Integer uselessInput) {
        long retries = 1L;
        long res;
        long total = 0L;
        do {
            res = lockCheckAndExec();
            log.debug("[DANGLING MEDIA] Run returned with {}", res);
            if (res < 0L) {
                try {
                    if (retries > 200L) {
                        log.error("[DANGLING MEDIA] Unable to start a work after {} tries", retries);
                        return total;
                    }
                    long sleepMs = 1000L * retries++;
                    log.debug("[DANGLING MEDIA] Cannot work rn, retrying in {}ms", sleepMs);
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                retries = 1L;
            }
            //Since deleting some objects may create new dangling media, we have to retry
            // until we converge at 0 dangling media deleted
            if (res > 0L) {
                total += res;
                try {
                    log.debug("[DANGLING MEDIA] Dangling media deleted some objects. Retry until convergence");
                    //Sleep a bit to not fully lock other threads
                    Thread.sleep(4000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } while (res != 0L);
        return total;
    }


    private long lockCheckAndExec() {
        //To prevent races between this and upload of medias, we have to be sure that:
        // - Nobody is uploading/updating/deleting medias (we guarantee this with the locks)
        // - Nobody is uploading new photos to the gallery (otherwise he will be stuck once completed)
        // - Gallery processor is idle (otherwise he can create new s3 objects not present in db)
        try {
            mediaWriteMutexLock();
            if (uploadProgressFinder.areUploadInProgress()) {
                log.debug("[DANGLING MEDIA] New uploads in progress");
                return -1L;
            }
            if (uploadFinder.areUploadStillProcessing()) {
                log.debug("[DANGLING MEDIA] Upload still processing");
                return -1L;
            }
            return exec();
        } finally {
            mediaWriteMutexUnlock();
        }
    }

    private long exec() {
        List<MediaData> medias = new LinkedList<>(mediaFinder.findAll());
        Set<Long> referencedMediaIds = mediaFinder.getReferencedMediaIds();
        Set<Long> dbDeleteIds = new HashSet<>();
        String basePath = storageConfig.getBasePublicPath();

        //LOCAL FILES
        log.debug("[DANGLING MEDIA] Loading local files");
        Set<String> localFiles = new HashSet<>();
        //Get paths
        int basePathLength = basePath.length();
        Path mediaPath = Paths.get(storageConfig.getFullMediaPath());
        //For each file
        try (Stream<Path> files = Files.find(mediaPath, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile())) {
            files.forEach(path -> localFiles.add(path.normalize().toString().substring(basePathLength)));
        } catch (IOException e) {
            log.error("[DANGLING MEDIA] Error while listing files: {}", e.getMessage());
        }

        //S3 REMOTE
        log.debug("[DANGLING MEDIA] Loading remote keys");
        Set<String> s3RemoteKeys = new HashSet<>();
        s3ListObjects.forEach(s3Object -> {
            String key = s3Object.key();
            s3RemoteKeys.add(key);
        });

        log.debug("[DANGLING MEDIA] Got {} local files and {} remote keys", localFiles.size(), s3RemoteKeys.size());

        log.debug("[DANGLING MEDIA] Checking for unreferenced db objects + db objects without stored file");
        long deleted = 0L;
        for (MediaData media : medias) {
            try {
                StoreMethod storeMethod = media.getStoreMethod();
                //Delete unreferenced media db objects
                if (!referencedMediaIds.contains(media.getId())) {
                    log.info("[DANGLING MEDIA] Deleting unreferenced db media: {}", media);
                    //Media will be deleted in subsequent calls if it's really not referenced.
                    //  This fixes a bug if two media object (one referenced) points to the same file:
                    //  By deleting the file immediately we would kill also for the other media object
                    //physicallyDeleteMediaAction.invoke(media, false);
                    dbDeleteIds.add(media.getId());
                    deleted++;
                    continue;
                }

                //Delete media db object without stored file
                if (storeMethod == StoreMethod.DISK) {
                    if (!localFiles.contains(media.getPath())) {
                        log.info("[DANGLING MEDIA] Deleting media without local file: {}", media);
                        dbDeleteIds.add(media.getId());
                        deleted++;
                    }
                    continue;
                }
                if (storeMethod == StoreMethod.S3_REMOTE) {
                    if (!s3RemoteKeys.contains(media.getPath())) {
                        log.info("[DANGLING MEDIA] Deleting media without s3 remote file: {}", media);
                        dbDeleteIds.add(media.getId());
                        deleted++;
                    }
                    continue;
                }
            } catch (Exception e) {
                log.warn("[DANGLING MEDIA] Error while checking for media {}", media);
            }
        }

        //Find stored media which are not present in the db and deletes them
        log.debug("[DANGLING MEDIA] Building sets of every path");
        //Build set of every path
        Set<String> allMediaOnDiskPaths = new HashSet<>();
        Set<String> allMediaOnRemoteS3Keys = new HashSet<>();
        medias.forEach(media -> {
            if (dbDeleteIds.contains(media.getId())) {
                log.debug("[DANGLING MEDIA] Skipping media {} because it's already deleted", media);
                return;
            }
            StoreMethod storeMethod = media.getStoreMethod();
            switch (storeMethod) {
                case DISK -> allMediaOnDiskPaths.add(media.getPath());
                case S3_REMOTE -> allMediaOnRemoteS3Keys.add(media.getPath());
                default -> {
                }
            }
        });
        log.debug("[DANGLING MEDIA] Found referenced {} media on disk and {} on s3 remote",
                allMediaOnDiskPaths.size(), allMediaOnRemoteS3Keys.size());

        // DISK STORAGE
        log.debug("[DANGLING MEDIA] Deleting medias in db without local file");
        Set<String> localWithoutDbObjects = new HashSet<>(localFiles);
        localWithoutDbObjects.removeAll(allMediaOnDiskPaths);
        localWithoutDbObjects.forEach(path -> {
            try {
                log.info("[DANGLING MEDIA] Deleting file without db object: {}", path);
                Files.deleteIfExists(Paths.get(basePath, path));
            } catch (IOException e) {
                log.error("[DANGLING MEDIA] Error while deleting file {} not present in db: {}",
                        path, e.getMessage());
            }
        });

        // S3 REMOTE STORAGE
        log.debug("[DANGLING MEDIA] Deleting medias in db without s3 remote file");
        Set<String> remoteWithoutDbObjects = new HashSet<>(s3RemoteKeys);
        remoteWithoutDbObjects.removeAll(allMediaOnRemoteS3Keys);
        remoteWithoutDbObjects.forEach(key -> {
            try {
                log.info("[DANGLING MEDIA] Deleting s3 remote object without db object: {}", key);
                s3DeleteUpload.delete(key);
            } catch (S3Exception e) {
                log.error("[DANGLING MEDIA] Error while deleting s3 remote object {} not present in db: {}",
                        key, e.getMessage());
            }
        });

        log.debug("[DANGLING MEDIA] Removing from db ids: {}", dbDeleteIds);
        deleteMediaAction.deleteFromDb(dbDeleteIds.stream().toList());

        return deleted;
    }
}
