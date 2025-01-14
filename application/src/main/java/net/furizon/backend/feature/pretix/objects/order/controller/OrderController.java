package net.furizon.backend.feature.pretix.objects.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.OrderWebhookRequest;
import net.furizon.backend.feature.pretix.objects.order.usecase.FetchSingleOrderUseCase;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.annotation.InternalAuthorize;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderController {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final PretixInformation pretixService;

    private static final ReentrantLock WEBHOOK_MUTEX = new ReentrantLock(true);

    @GetMapping("/ping")
    @InternalAuthorize
    public boolean internalPing() {
        return true;
    }

    private static final ResponseEntity<Void> OK = ResponseEntity.ok().build();
    private static final ResponseEntity<Void> NOP = ResponseEntity.internalServerError().build();

    @Operation(
        summary = "A pretix order has changed",
        description = "Each time a pretix order changes, pretix will hit this webhook with the information "
            + "about the order we need to update in db. This method must be used ONLY by pretix, it's not meant "
            + "to be exposed to public API"
    )
    @PostMapping("/webhook")
    @InternalAuthorize
    public ResponseEntity<Void> pretixWebhook(@Valid @NotNull @RequestBody OrderWebhookRequest request) {
        try {
            WEBHOOK_MUTEX.lock();
            log.info("[PRETIX WEBHOOK] Fetching order {}", request);
            Event event = pretixService.getCurrentEvent();

            String slug = PretixGenericUtils.buildOrgEventSlug(request.getEvent(), request.getOrganizer());

            if (!event.getSlug().equals(slug)) {
                log.error("[PRETIX WEBHOOK] Webhook request is not for the current event!");
                return ResponseEntity.internalServerError().build();
            }

            var res = useCaseExecutor.execute(FetchSingleOrderUseCase.class, new FetchSingleOrderUseCase.Input(
                    event,
                    request.getCode(),
                    pretixService
            ));
            return OK;
            //return res.isPresent() ? OK : NOP;
        } finally {
            WEBHOOK_MUTEX.unlock();
        }
    }

    //This is static for making a shitty and fast workaround for dependency loops
    //Tomorrow morning we should open registration for staff, but upgrade room bug isn't fixed yet :(
    public static void suspendWebhook() {
        log.info("[PRETIX WEBHOOK] Suspending webhook");
        WEBHOOK_MUTEX.lock();
        log.info("[PRETIX WEBHOOK] Webhook suspended");
    }
    public static void resumeWebhook() {
        log.info("[PRETIX WEBHOOK] Resuming webhook");
        WEBHOOK_MUTEX.unlock();
    }
}
