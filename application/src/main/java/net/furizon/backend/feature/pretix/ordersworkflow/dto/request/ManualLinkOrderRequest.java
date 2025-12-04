package net.furizon.backend.feature.pretix.ordersworkflow.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManualLinkOrderRequest {
    @NotNull @NotEmpty @Size(min = 4, max = 48)
    public final String orderCode;
    @NotNull
    public final Long userId;
}
