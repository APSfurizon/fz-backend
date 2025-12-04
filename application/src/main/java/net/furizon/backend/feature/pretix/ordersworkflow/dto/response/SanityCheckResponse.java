package net.furizon.backend.feature.pretix.ordersworkflow.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class SanityCheckResponse {
    @NotNull
    private List<OrderWorkflowErrorCode> errors;
}
