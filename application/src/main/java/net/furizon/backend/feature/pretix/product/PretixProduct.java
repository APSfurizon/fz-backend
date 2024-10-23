package net.furizon.backend.feature.pretix.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.Const;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Data
public class PretixProduct {

    private final int id;

    @JsonProperty("meta_data")
    @NotNull
    private final Map<String, String> metadata;

    @Nullable
    private final String name;

    @NotNull
    private final List<PretixProductVariation> variations;

    @Nullable
    public String getIdentifier() {
        return metadata.get(Const.METADATA_IDENTIFIER_ITEM);
    }

    public void forEachVariationByIdentifierPrefix(String prefix, BiConsumer<PretixProductVariation, String> callback) {
        variations.stream().filter(
                v -> v.getIdentifier() != null && v.getIdentifier().startsWith(prefix)
        ).forEach(v -> callback.accept(v, v.getIdentifier().substring(prefix.length())));
    }
}
