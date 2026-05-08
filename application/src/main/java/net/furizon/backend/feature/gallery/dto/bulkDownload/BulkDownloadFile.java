package net.furizon.backend.feature.gallery.dto.bulkDownload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

@Data
@Builder
public class BulkDownloadFile {
    @JsonProperty("k")
    @NotNull private final String s3Key;
    @JsonProperty("nf")
    @NotNull private String fileName;
    @JsonProperty("ne")
    @NotNull private String eventName;
    @JsonProperty("np")
    @NotNull private String photographerName;
    @JsonProperty("t")
    @NotNull private OffsetDateTime uploadTs;
    @JsonProperty("ip")
    private final long photographerId;
    @JsonProperty("ie")
    private final long eventId;
    @JsonProperty("s")
    private final long fileSize;
}
