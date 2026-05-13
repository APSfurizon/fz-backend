package net.furizon.backend.feature.gallery.dto.bulkDownload;

import lombok.Data;

import java.util.List;

@Data
public class BulkDirectDownloadResponse {
    private final List<BulkDirectDownloadFile> files;
}
