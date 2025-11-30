package net.furizon.backend.feature.pretix.objects.order.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatePretixPositionRequest {
    @NotNull
    private final String order;

    private final long item;

    @NotNull
    private final String price;

    @Nullable
    private final List<PretixAnswer> answers;

    public UpdatePretixPositionRequest(@NotNull String order, long item, long price) {
        this.order = order;
        this.item = item;
        this.price = PretixGenericUtils.fromPriceToString(price, '.');
        answers = null;
    }
}
