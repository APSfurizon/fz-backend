package net.furizon.backend.feature.organizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixOrganizer {
    @NotNull
    private final String name;

    @NotNull
    private final String slug;

    @JsonProperty("public_url")
    @NotNull
    private final String publicUrl;
}
