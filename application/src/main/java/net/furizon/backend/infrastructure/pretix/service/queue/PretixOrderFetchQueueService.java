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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannelSpec;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.QueueChannelSpec;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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
        //QueueChannel queue = new QueueChannel();
        //ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        //scheduler.setPoolSize(1); // Crucial: sets the pool size to 1 thread
        //scheduler.setThreadNamePrefix("pretix-queue-poller-");
        //queue.setTaskScheduler(scheduler);
        //return queue;
        return MessageChannels.queue().datatype(String.class);
    }

    @Bean("pretixOrderQueueFlow")
    public IntegrationFlow channelFlow() {
        return IntegrationFlow.from(pretixOrderQueueChannel())
                .filter(pendingIds::add)
                .handle(this::handleRequest)
                .get();
    }


    private synchronized void handleRequest(@NotNull Message<?> message) {
        String orderCode = (String) message.getPayload();
        pendingIds.remove(orderCode);
        Event event = pretixInformation.getCurrentEvent();
        log.info("[PRETIX_ORDER_QUEUE] Working on order {}", orderCode);
        int x = 431321;
        for (long i = 0L; i < 2999999990L; i++) {x = x / 2 * 3;} //About 10 secs of waiting time
        //try {
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}
        log.info("[PRETIX_ORDER_QUEUE] DONE order {} {}", orderCode, x);
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
