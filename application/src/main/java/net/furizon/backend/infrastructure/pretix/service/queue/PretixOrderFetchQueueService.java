package net.furizon.backend.infrastructure.pretix.service.queue;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.dto.request.OrderWebhookRequest;
import net.furizon.backend.feature.pretix.objects.order.usecase.FetchSingleOrderUseCase;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannelSpec;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.QueueChannelSpec;
import org.springframework.integration.filter.MessageFilter;
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
        return MessageChannels.queue().datatype(String.class); // capacità max
    }

    @Bean("pretixOrderQueueFlow")
    public IntegrationFlow channelFlow() {
        return IntegrationFlow.from(CHANNEL_BEAN_NAME)
                //.filter("pretixOrderQueueFilter")
                .filter(pendingIds::add)
                .handle(this::handleRequest)
                .get();
    }

    private void handleRequest(@NotNull Message<?> message) {
        String orderCode = (String) message.getPayload();
        pendingIds.remove(orderCode);
        Event event = pretixInformation.getCurrentEvent();
        log.info("[PRETIX_ORDER_QUEUE] Working on order {}", orderCode);
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //try {
        //    OrderController.suspendWebhook();
        //    useCaseExecutor.execute(FetchSingleOrderUseCase.class, new FetchSingleOrderUseCase.Input(
        //            event,
        //            orderCode,
        //            pretixInformation
        //    ));
        //} finally {
        //    OrderController.resumeWebhook();
        //}
    }
}
