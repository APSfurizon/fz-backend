package net.furizon.backend.feature.pretix.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.dto.OrderWebhookRequest;
import net.furizon.backend.feature.pretix.order.usecase.FetchSingleOrderUseCase;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderController {
    private final UseCaseExecutor useCaseExecutor;

    private final PretixInformation pretixService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> pretixWebhook(OrderWebhookRequest request) {
        log.info("[PRETIX WEBHOOK] Fetching order {}", request);
        var e = pretixService.getCurrentEvent();
        if (e.isPresent()) {
            String slug = PretixGenericUtils.buildOrgEventSlug(request.getEvent(), request.getOrganizer());
            Event event = e.get();

            if (event.getSlug().equals(slug)) {
                var res = useCaseExecutor.execute(FetchSingleOrderUseCase.class, new FetchSingleOrderUseCase.Input(
                    event,
                    request.getCode(),
                    pretixService
                ));
                return res.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
            } else {
                log.error("[PRETIX WEBHOOK] Webhook request is not for the current event!");
            }
        } else {
            log.error("[PRETIX WEBHOOK] No current event set!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
