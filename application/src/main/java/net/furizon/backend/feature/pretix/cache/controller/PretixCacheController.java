package net.furizon.backend.feature.pretix.cache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/cache/pretix")
@RequiredArgsConstructor
//TODO: Only admins are allowed to run these!
public class PretixCacheController {
    private final PretixInformation pretixService;

    @PostMapping("/reload-struct")
    public void reloadCache() {
        log.info("[PRETIX] Manual reload of cache");
        pretixService.resetCache();
    }

    @PostMapping("/reload-orders")
    public void reloadOrders() {
        log.info("[PRETIX] Manual reload of orders");
        pretixService.reloadAllOrders();
    }
}
