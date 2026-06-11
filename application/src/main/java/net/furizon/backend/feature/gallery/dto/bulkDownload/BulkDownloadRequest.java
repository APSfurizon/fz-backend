package net.furizon.backend.feature.gallery.dto.bulkDownload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkDownloadRequest {
    @NotNull private final List<Long> ids;
}
