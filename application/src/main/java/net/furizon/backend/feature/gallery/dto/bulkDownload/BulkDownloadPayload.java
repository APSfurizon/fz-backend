package net.furizon.backend.feature.gallery.dto.bulkDownload;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class BulkDownloadPayload {
    @NotNull List<BulkDownloadFile> files;
    private final long expiryMs;
    private final long userId;
}
