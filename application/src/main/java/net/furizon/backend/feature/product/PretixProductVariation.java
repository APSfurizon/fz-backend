package net.furizon.backend.feature.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.Const;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
public class PretixProductVariation {

    private final int id;

    @Nullable
    private final String name;

    @JsonProperty("meta_data")
    @NotNull
    private final Map<String, String> metadata;

    @Nullable
    public String getIdentifier() {
        return metadata.get(Const.METADATA_IDENTIFIER_ITEM);
    }
}
