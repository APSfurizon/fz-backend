package net.furizon.backend.feature.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class PretixEvent {
    // This is key-value field (ex: {"en": "eventName"})
    @NotNull
    private final Map<String, String> name;

    @NotNull
    private final String slug;

    @JsonProperty("date_from")
    @Nullable
    private final OffsetDateTime dateFrom;

    @JsonProperty("date_to")
    @Nullable
    private final OffsetDateTime dateTo;

    @JsonProperty("public_url")
    @NotNull
    private final String publicUrl;
}
