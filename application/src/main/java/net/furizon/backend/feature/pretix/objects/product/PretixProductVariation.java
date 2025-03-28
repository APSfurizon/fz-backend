package net.furizon.backend.feature.pretix.objects.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
public class PretixProductVariation {
    private final long id;
    private final boolean active;

    @NotNull
    @JsonProperty("value")
    private final Map<String, String> names;

    @NotNull
    @JsonProperty("meta_data")
    private final Map<String, String> metadata;

    @NotNull
    private final String price;

    @Nullable
    public String getIdentifier() {
        return metadata.get(PretixConst.METADATA_IDENTIFIER_ITEM);
    }

    @NotNull
    public Map<String, String> getCustomNames() {
        return PretixGenericUtils.convertCustomNames(metadata.get(PretixConst.METADATA_IDENTIFIER_CUSTOM_NAME));
    }
}
