package net.furizon.backend.feature.pretix.objects.order;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixOrder {
    @NotNull
    private final String code;

    @NotNull
    private final String secret;

    @NotNull
    private final String status;

    @NotNull
    private final List<PretixPayment> payments;

    @NotNull
    private final List<PretixPosition> positions;
}
