package net.furizon.backend.feature.pretix.cache.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/cache/pretix")
@RequiredArgsConstructor
public class PretixCacheController {
    @NotNull
    private final PretixInformation pretixService;

    @Operation(
        summary = "Reloads the pretix's events, organizers, questions and products",
        description = "Reloads the whole list of events and organizers from pretix and then, "
            + "for the current event, refreshes the list of questions and products, "
            + "by compiling again the list of \"backend\" products. This method is meant "
            + "to be launched ONLY by the backend admins"
    )
    @PermissionRequired(permissions = {Permission.CAN_REFRESH_PRETIX_CACHE})
    @PostMapping("/reload-struct")
    public void reloadCache() {
        log.info("[PRETIX] Manual reload of cache");
        pretixService.resetCache();
    }

    @Operation(
        summary = "Refetches the full order list and updates them in the db",
        description =
        "This method is meant to be launched ONLY by the backend admins"
    )
    @PermissionRequired(permissions = {Permission.CAN_REFRESH_PRETIX_CACHE})
    @PostMapping("/reload-orders")
    public void reloadOrders() {
        log.info("[PRETIX] Manual reload of orders");
        pretixService.reloadAllOrders();
    }
}
