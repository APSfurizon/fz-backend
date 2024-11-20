package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.SanityCheckResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.GeneratePretixShopLink;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.GetEditOrderLink;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.SanityCheck;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders-workflow")
@RequiredArgsConstructor
public class OrdersWorkflowController {
    @NotNull private final UseCaseExecutor executor;
    @NotNull private final PretixInformation pretixService;
    @NotNull private final SanityCheck sanityCheck;

    @GetMapping("/generate-pretix-shop-link")
    public LinkResponse generatePretixShopLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GeneratePretixShopLink.class,
                new GeneratePretixShopLink.Input(user, pretixService)
        );
    }

    @GetMapping("/get-order-edit-link")
    public LinkResponse getOrderEditLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetEditOrderLink.class,
                new GetEditOrderLink.Input(user, pretixService)
        );
    }

    @GetMapping("/order-sanity-check")
    public SanityCheckResponse doSanityChecks(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return sanityCheck.execute(user, pretixService);
    }
}
