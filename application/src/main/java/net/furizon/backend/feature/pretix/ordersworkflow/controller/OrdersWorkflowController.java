package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.*;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.*;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders-workflow")
@RequiredArgsConstructor
public class OrdersWorkflowController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixService;
    @org.jetbrains.annotations.NotNull
    private final SanityCheck sanityCheck;

    @Operation(summary = "Generates an user personalized link for the pretix shop", description =
        "Generates a link to the pretix shop which contains the parameters for pretixautocart, "
        + "so the order's products and questions are automatically filled. This link should be used by the frontend "
        + "to show buttons or redirect the user. This button should be showed only if the user has no order")
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
        "Generates a link to the pretix shop which points to the user's order page, so he can change it. "
        + "This link should be showed only if the user has a registered order. "
        + "This link should be used by the frontend to show buttons or redirect the user")
    @GetMapping("/get-order-edit-link")
    public LinkResponse getOrderEditLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetEditOrderLink.class,
                new GetEditOrderLink.Input(user, pretixService)
        );
    }

    @Operation(summary = "Returns a link for retrying the payment of the user's order", description =
            "Generates a link to the pretix shop which points to the user's order retry payment page. "
                    + "This link should be showed only if the user has an order in the PENDING state. "
                    + "This link should be used by the frontend to show buttons or redirect the user")
    @GetMapping("/get-order-retry-payment-link")
    public LinkResponse getRetryPaymentLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetPayOrderLink.class,
                new GetPayOrderLink.Input(user, pretixService)
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
        return new SanityCheckResponse(sanityCheck.execute(user, pretixService));
    }

    @Operation(summary = "Gets all user's order information aggregated in one call", description =
        "Returns all user's information that should be displayed in the frontend's user page. "
        + "If `shouldDisplayCountdown` is `true`, the frontend should display a countdown ending "
        + "at `bookingStartTime`. After the countdown is finished, a link/button pointing at "
        + "[`/generate-pretix-shop-link`](#/orders-workflow-controller/generatePretixShopLink) "
        + "should be displayed instead. Once an user has an order, instead of the shop link the "
        + "frontend should show a link to [`/get-edit-order-link`](#/orders-workflow-controller/getOrderEditLink). "
        + "After `editBookEndTime` we don't have to show anything but a notice, "
        + "since the edit of the orders is disabled pretix side. `eventNames` contains a map "
        + "language->name of the name of the event. Both `eventNames` and `hasActiveMembershipForEvent` should "
        + "be displayed regardless if the user owns an order or not. `errors` contains the same list of errors "
        + "you can find in the [`/order-sanity-check`](#/orders-workflow-controller/doSanityChecks) method. "
        + "Refer to its documentation for a full description. In the order object, `dailyDays` is a set of "
        + "indexes of the days the user has bought. `roomTypeNames` is a map language->name of the name "
        + "of the room.")
    @GetMapping("/get-full-status")
    public FullInfoResponse getFullStatus(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        FullInfoResponse r = executor.execute(
                GenerateFullStatusUseCase.class,
                new GenerateFullStatusUseCase.Input(
                        pretixService,
                        user
                )
        );
        //Zozzating
        r.setShouldUpdateInfo(executor.execute(
                CheckIfUserShouldUpdateInfoUseCase.class,
                new CheckIfUserShouldUpdateInfoUseCase.Input(
                    user,
                    pretixService.getCurrentEvent()
                )
            )
        );
        return r;
    }

    @Operation(summary = "Links an order to a user", description =
            "When a user makes an order, changes an order or cancels the change of an order, it is "
            + "automatically redirected to a page in the frontend, configurable from pretix itself, "
            + "with the order's params in the query: `?c={code}&s={secret}&m={message}`. "
            + "This page should call this method to link the order to the logged in user. "
            + "After calling this page the frontend should redirect the user to the order home page, "
            + "displaying a popup with the results of this operation. "
            + "This operation can drop the following errors: `SERVER_ERROR`, `ORDER_SECRET_NOT_MATCH`, "
            + " `ORDER_MULTIPLE_DONE` and `ORDER_NOT_FOUND`.")
    @PostMapping("/link-order")
    public boolean linkOrder(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @NotNull @Valid @RequestBody final LinkOrderRequest request
    ) {
        return executor.execute(
                RegisterUserOrder.class,
                new RegisterUserOrder.Input(
                        user.getUserId(),
                        true,
                        false,
                        request.getOrderCode(),
                        request.getOrderSecret(),
                        pretixService
                )
        );

    }
    @Operation(summary = "Links the specified order to the specified user", description =
        "This methods is only intended for an admin use")
    @PermissionRequired(permissions = {Permission.PRETIX_ADMIN})
    @PostMapping("/manual-order-link")
    public boolean manualOrderLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final ManualLinkOrderRequest request
    ) {
        log.info("User {} is trying to link order {} to user {}",
                user.getUserId(), request.getOrderCode(), request.getUserId());
        return executor.execute(
                RegisterUserOrder.class,
                new RegisterUserOrder.Input(
                        user.getUserId(),
                        false,
                        true,
                        request.getOrderCode(),
                        null,
                        pretixService
                )
        );
    }

    @Operation(summary = "Gets the link to the pretix control page for the specified order", description =
        "Order must be expressed NOT by their code, but their db id. Only allowed for admins")
    @PermissionRequired(permissions = {Permission.PRETIX_ADMIN})
    @GetMapping("/generate-pretix-shop-order-link")
    public LinkResponse generatePretixShopOrderLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @NotNull @RequestParam("id") final Long orderId
    ) {
        return executor.execute(
            GeneratePretixShopOrderLinkUseCase.class,
                new GeneratePretixShopOrderLinkUseCase.Input(
                        orderId,
                        pretixService
                )
        );
    }

}
