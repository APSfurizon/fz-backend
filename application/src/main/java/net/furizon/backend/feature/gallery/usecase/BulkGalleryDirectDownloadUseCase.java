package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

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
