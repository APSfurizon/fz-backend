package net.furizon.backend.feature.gallery.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadFile;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadPayload;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.generalUtils.hmacEncoder.HmacEncoder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.hc.client5.http.utils.Hex;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkGalleryDownloadUseCase implements UseCase<BulkGalleryDownloadUseCase.Input, BulkDownloadResponse> {
    @NotNull
    private final UploadFinder uploadFinder;

    @NotNull
    private final HmacEncoder hmacEncoder;

    @NotNull
    private final GalleryConfig config;

    @Override
    public @NotNull BulkDownloadResponse executor(@NotNull BulkGalleryDownloadUseCase.Input input) {
        GalleryConfig.BatchDownload batchDownloadConfig = config.getBatchDownload();
        log.info("User {} is bulk downloading files {}", input.user.getUserId(), input.ids);
        List<BulkDownloadFile> files = uploadFinder.getBulkDownloadableFiles(input.ids);

        //NAMES FIXING
        //sanitize event and fursona names
        sanitizeNames(
            files,
            BulkDownloadFile::getEventName,
            BulkDownloadFile::setEventName
        );
        sanitizeNames(
            files,
            BulkDownloadFile::getPhotographerName,
            BulkDownloadFile::setPhotographerName
        );

        //Prepend id in front of each name
        prependIdToNames(
            files,
            BulkDownloadFile::getEventId,
            BulkDownloadFile::getEventName,
            BulkDownloadFile::setEventName
        );
        prependIdToNames(
            files,
            BulkDownloadFile::getPhotographerId,
            BulkDownloadFile::getPhotographerName,
            BulkDownloadFile::setPhotographerName
        );

        // Remove name duplicates
        resolveDuplicates(
            files,
            BulkDownloadFile::getEventId,
            BulkDownloadFile::getEventName,
            BulkDownloadFile::setEventName
        );
        resolveDuplicates(
            files,
            BulkDownloadFile::getPhotographerId,
            BulkDownloadFile::getPhotographerName,
            BulkDownloadFile::setPhotographerName
        );

        BulkDownloadPayload payload = new BulkDownloadPayload(
                files,
                OffsetDateTime.now().plusMinutes(batchDownloadConfig.getExpireMins()).toInstant().toEpochMilli(),
                input.user.getUserId()
        );
        String b64Str;
        String hmacStr;
        try {
            var encoded = hmacEncoder.encode(payload, batchDownloadConfig.getHmacKey());
            b64Str = new String(Base64.encode(encoded.data().getBytes()));
            hmacStr = Hex.encodeHexString(encoded.hmacOut());
        } catch (JsonProcessingException e) {
            log.error("Error while processing payload");
            throw new RuntimeException(e);
        }

        return new BulkDownloadResponse(
                batchDownloadConfig.getDownloadUrl(),
                hmacStr,
                b64Str
        );
    }

    private final void sanitizeNames(
            @NotNull List<BulkDownloadFile> files,
            @NotNull Function<BulkDownloadFile, String> nameGetter,
            @NotNull BiConsumer<BulkDownloadFile, String> nameSetter
    ) {
        files.forEach(f ->
                nameSetter.accept(f, nameGetter.apply(f).replaceAll("[^\\p{L}\\p{N}\\p{M}_\\-'()\\[\\]. ]", "")));
    }

    private final void prependIdToNames(
            @NotNull List<BulkDownloadFile> files,
            @NotNull Function<BulkDownloadFile, Long> idGetter,
            @NotNull Function<BulkDownloadFile, String> nameGetter,
            @NotNull BiConsumer<BulkDownloadFile, String> nameSetter
    ) {
        files.forEach(f -> nameSetter.accept(f, String.format("%04d-%s", idGetter.apply(f), nameGetter.apply(f))));
    }

    private final void resolveDuplicates(
            @NotNull List<BulkDownloadFile> files,
            @NotNull Function<BulkDownloadFile, Long> idGetter,
            @NotNull Function<BulkDownloadFile, String> nameGetter,
            @NotNull BiConsumer<BulkDownloadFile, String> nameSetter
    ) {
        //Which names are shared by which id
        Map<String, Set<Long>> nameToIds = new HashMap<>();
        //Which files has the ID (for easier update)
        Map<Long, List<BulkDownloadFile>> idToFiles = new HashMap<>();
        //Build the maps
        files.forEach(f -> {
            String name = nameGetter.apply(f);
            long id = idGetter.apply(f);

            Set<Long> ids = nameToIds.computeIfAbsent(name, s -> new HashSet<Long>());
            ids.add(id);

            List<BulkDownloadFile> fs = idToFiles.computeIfAbsent(id, l -> new ArrayList<>());
            fs.add(f);
        });
        Set<String> names = nameToIds.keySet();

        //Actual change the names
        nameToIds.forEach((name, ids) -> {
            //If a name is shared by more than one id
            if (ids.size() > 1) {
                //Per each id, try to make it unique
                for (long id : ids) {
                    int i = 0;
                    String finalName;
                    do {
                        finalName = String.format("%s-%02d", name, i++);
                    } while (names.contains(finalName));
                    //A new unique name is found
                    final String uniqueName = finalName;
                    names.add(uniqueName);
                    idToFiles.get(id).forEach(f -> nameSetter.accept(f, uniqueName));
                }
            }
        });
    }

    public record Input(
            @NotNull Set<Long> ids,
            @NotNull FurizonUser user
    ) {}
}
