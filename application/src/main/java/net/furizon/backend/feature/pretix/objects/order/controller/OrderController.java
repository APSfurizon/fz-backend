package net.furizon.backend.feature.pretix.objects.order.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderController {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final PretixInformation pretixService;

    @GetMapping("/ping")
    @InternalAuthorize
    public boolean internalPing() {
        return true;
    }

    @Operation(
        summary = "A pretix order has changed",
        description = "Each time a pretix order changes, pretix will hit this webhook with the information "
            + "about the order we need to update in db. This method must be used ONLY by pretix, it's not meant "
            + "to be exposed to public API"
    )
    @PostMapping("/webhook")
    @InternalAuthorize
    public ResponseEntity<Void> pretixWebhook(OrderWebhookRequest request) {
        log.info("[PRETIX WEBHOOK] Fetching order {}", request);
        var e = pretixService.getCurrentEvent();
        if (e.isEmpty()) {
            log.error("[PRETIX WEBHOOK] No current event set!");
            return ResponseEntity.internalServerError().build();
        }

        String slug = PretixGenericUtils.buildOrgEventSlug(request.getEvent(), request.getOrganizer());
        Event event = e.get();

        if (!event.getSlug().equals(slug)) {
            log.error("[PRETIX WEBHOOK] Webhook request is not for the current event!");
            return ResponseEntity.internalServerError().build();
        }

        var res = useCaseExecutor.execute(FetchSingleOrderUseCase.class, new FetchSingleOrderUseCase.Input(
            event,
            request.getCode(),
            pretixService
        ));
        return res.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
    }
}
