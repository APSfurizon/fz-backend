package net.furizon.backend.feature.pretix.objects.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
public class PretixProduct {
    private final long id;
    private final boolean active;

    @NotNull
    @JsonProperty("default_price")
    private final String price;

    @NotNull
    @JsonProperty("meta_data")
    private final Map<String, String> metadata;

    @NotNull
    @JsonProperty("name")
    private final Map<String, String> names;

    @NotNull
    private final List<PretixProductVariation> variations;

    @NotNull
    private final List<PretixProductBundle> bundles;

    @Nullable
    public String getIdentifier() {
        return metadata.get(PretixConst.METADATA_IDENTIFIER_ITEM);
    }

    public long getLongPrice() {
        return PretixGenericUtils.fromStrPriceToLong(getPrice());
    }

    @NotNull
    public Map<String, String> getCustomNames() {
        return PretixGenericUtils.convertCustomNames(metadata.get(PretixConst.METADATA_IDENTIFIER_CUSTOM_NAME));
    }

    public void forEachVariationByIdentifierPrefix(
        @NotNull String prefix,
        @NotNull TriConsumer<PretixProductVariation, String, Long> callback
    ) {
        variations.stream()
            .filter(v -> v.isActive() && v.getIdentifier() != null && v.getIdentifier().startsWith(prefix))
            .forEach(v -> callback.accept(v, v.getIdentifier().substring(prefix.length()), v.getId()));
    }
}
