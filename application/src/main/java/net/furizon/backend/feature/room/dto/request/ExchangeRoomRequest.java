package net.furizon.backend.feature.room.dto.request;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
public class ExchangeRoomRequest {
    @NotNull
    private final String sourceOrderCode;
    private final long sourceRootPositionId;

    @NotNull
    private final String destOrderCode;
    private final long destRootPositionId;

    @Nullable
    private final String manualPaymentComment;
    @Nullable
    private final String manualRefundComment;

    @NotNull
    @Builder.Default
    private final List<Exchange> exchanges = new LinkedList<Exchange>();

    public static class ExchangeRoomRequestBuilder {
        public ExchangeRoomRequestBuilder exchange(@Nullable Long sourcePositionId, @Nullable Long destPositionId) {
            return exchange(new Exchange(sourcePositionId, destPositionId));
        }
        public ExchangeRoomRequestBuilder exchange(@NotNull Exchange exchange) {
            if (this.exchanges$value == null) {
                this.exchanges$value = new LinkedList<Exchange>();
            }
            this.exchanges$value.add(exchange);
            this.exchanges$set = true;
            return this;
        }
    }

    @Data
    public static class Exchange {
        @Nullable
        private final Long sourcePositionId;
        @Nullable
        private final Long destPositionId;
    }
}
