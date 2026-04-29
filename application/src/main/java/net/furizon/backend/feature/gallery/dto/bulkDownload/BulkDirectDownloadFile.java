package net.furizon.backend.feature.gallery.dto.bulkDownload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

@Data
public class BulkDirectDownloadFile {
    @JsonProperty("u")
    @NotNull private final String downloadUrl;
    @JsonProperty("n")
    @NotNull private final String fileName;
    @JsonProperty("t")
    @NotNull private final OffsetDateTime uploadTs;
    @JsonProperty("s")
    private final long fileSize;
}
