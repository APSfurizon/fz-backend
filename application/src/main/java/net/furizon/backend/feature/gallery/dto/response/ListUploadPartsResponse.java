package net.furizon.backend.feature.gallery.dto.response;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class ListUploadPartsResponse {
    @NotNull private final List<Integer> alreadyUploadedParts;
}
