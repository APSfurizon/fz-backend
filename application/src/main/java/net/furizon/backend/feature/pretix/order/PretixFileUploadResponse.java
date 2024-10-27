package net.furizon.backend.feature.pretix.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class PretixFileUploadResponse {
    @NotNull
    private String id;
}
