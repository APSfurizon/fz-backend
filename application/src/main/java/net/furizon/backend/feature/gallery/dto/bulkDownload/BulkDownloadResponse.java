package net.furizon.backend.feature.gallery.dto.bulkDownload;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class BulkDownloadResponse {
    @NotNull private final String url;
    @NotNull private final String body;

    public BulkDownloadResponse(
            @NotNull String baseUrl,
            @NotNull String hmac,
            @NotNull String body
    ) {
        this.url = baseUrl + hmac;
        this.body = body;
    }
}
