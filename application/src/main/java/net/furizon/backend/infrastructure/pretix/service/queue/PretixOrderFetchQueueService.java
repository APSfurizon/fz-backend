package net.furizon.backend.infrastructure.pretix.service.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.usecase.FetchSingleOrderUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.QueueChannelSpec;
import org.springframework.messaging.Message;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PretixOrderFetchQueueService {
    public static final String CHANNEL_BEAN_NAME = "pretixOrderQueueChannel";

    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final PretixInformation pretixInformation;


    private final Set<String> pendingIds = ConcurrentHashMap.newKeySet();

    @Bean(CHANNEL_BEAN_NAME)
    public QueueChannelSpec pretixOrderQueueChannel() {
        return MessageChannels.queue().datatype(String.class);
    }

    @Bean("pretixOrderQueueFlow")
    public IntegrationFlow channelFlow() {
        return IntegrationFlow.from(pretixOrderQueueChannel())
                .filter(
                    pendingIds::add,
                    filter -> filter.discardFlow(
                        f -> f.handle(
                            m -> log.warn("[PRETIX_ORDER_QUEUE] Order {} already in queue", m.getPayload())
                        )
                    )
                )
                .handle(this::handleRequest)
                .get();
    }


    private void handleRequest(@NotNull Message<?> message) {
        String orderCode = (String) message.getPayload();
        Event event = pretixInformation.getCurrentEvent();

        try {
            OrderController.suspendWebhook();
            log.info("[PRETIX_ORDER_QUEUE] Working on order {}", orderCode);
            pendingIds.remove(orderCode);
            useCaseExecutor.execute(FetchSingleOrderUseCase.class, new FetchSingleOrderUseCase.Input(
                    event,
                    orderCode,
                    pretixInformation
            ));
        } finally {
            OrderController.resumeWebhook();
        }
        log.info("[PRETIX_ORDER_QUEUE] DONE order {}", orderCode);
    }
}
