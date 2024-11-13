package net.furizon.backend.feature.pretix.cache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/pretixcache")
@RequiredArgsConstructor
//TODO: Only admins are allowed to run these!
public class CacheController {
    private final PretixInformation pretixService;

    @PostMapping("/reloadstruct")
    public void reloadCache() {
        log.info("[PRETIX] Manual reload of cache");
        pretixService.resetCache();
    }

    @PostMapping("/reloadorders")
    public void reloadOrders() {
        log.info("[PRETIX] Manual reload of orders");
        pretixService.reloadAllOrders();
    }
}
