package net.furizon.backend.feature.gallery.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadResponse;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadFile;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadPayload;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.hc.client5.http.utils.Hex;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkGalleryDirectDownloadUseCase implements
        UseCase<BulkGalleryDirectDownloadUseCase.Input, BulkDirectDownloadResponse> {
    @NotNull
    private final UploadFinder uploadFinder;

    @Override
    public @NotNull BulkDirectDownloadResponse executor(@NotNull BulkGalleryDirectDownloadUseCase.Input input) {
        log.info("User {} is bulk downloading files {}", input.user.getUserId(), input.ids);
        return new BulkDirectDownloadResponse(
                uploadFinder.getBulkDirectDownloadableFiles(input.ids)
        );
    }

    public record Input(
            @NotNull Set<Long> ids,
            @NotNull FurizonUser user
    ) {}
}
