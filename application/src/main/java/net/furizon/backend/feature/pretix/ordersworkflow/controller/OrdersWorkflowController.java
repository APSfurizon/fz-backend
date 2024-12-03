package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Generates an user personalized link for the pretix shop", description =
        "Generates a link to the pretix shop which contains the parameters for pretixautocart, "
        + "so the order's products and questions are automatically filled. This link should be used by the frontend "
        + "to show buttons or redirect the user")
    @GetMapping("/generate-pretix-shop-link")
    public LinkResponse generatePretixShopLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GeneratePretixShopLink.class,
                new GeneratePretixShopLink.Input(user, pretixService)
        );
    }

    @Operation(summary = "Returns a link to the pretix shop where an user can edit his order", description =
        "Generates a link to the pretix shop which points to the user's order page, so he can change it."
        + "This order should be used by the frontend to show buttons or redirect the user")
    @GetMapping("/get-order-edit-link")
    public LinkResponse getOrderEditLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetEditOrderLink.class,
                new GetEditOrderLink.Input(user, pretixService)
        );
    }

    @Operation(summary = "Checks if everything is ok with the user's order and membership card", description =
        "This request returns a list of errors. It checks if the user has multiple order registered "
        + "for the current event, has multiple membership cards registered for the year of the current event and "
        + "if the user has an order for the current event, but no registered membership card. "
        + "The first error should be frontend-blocking, the user should not have the possibility of doing anything "
        + "until he solves the problem with an admin. The second error should prompt a message where it says to "
        + "contact an admin to solve it. The third error should prompt an error where it says to buy a "
        + "membership card and a link for doing so + user should be blocked from performing "
        + "some operations like room management."
    )
    @GetMapping("/order-sanity-check")
    public SanityCheckResponse doSanityChecks(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return sanityCheck.execute(user, pretixService);
    }
}
