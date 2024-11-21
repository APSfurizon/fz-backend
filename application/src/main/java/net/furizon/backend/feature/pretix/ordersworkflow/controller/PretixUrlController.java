package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.RegisterUserOrder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/*
There are some GET requests initialized by the user which normally would go to pretix,
but we catch them from the reverse proxy. This class is responsible for handling them
 */
@RestController
@RequestMapping("${pretix.shop.path}")
@RequiredArgsConstructor
public class PretixUrlController {
    @NotNull
    private final UseCaseExecutor executor;
    @NotNull
    private final PretixInformation pretixService;

    //hmac is used by pretix to confirm an user has opened the link in his email.
    //it is sent ONLY through emails and when an user opens a link with it
    //it's used to check if the email ower IS the order owner.
    //If yes, a flag is set inside the order that the email was verified
    //so that pretix admins can search thru "email confirmed" orders
    //TLDR; For us it's useless :D
    @Operation(summary = "Links an order to a user and redirects to home page", description =
        "When a user makes an order, changes an order or cancels the change of an order, it is "
        + "automatically redirected to this page. Using a reverse proxy this *pretix* page should be "
        + "redirected to the backend. Whenever an user loads this page, the backend should use its "
        + "session to link the order to the user, and later redirect the user to the frontend "
        + "order page. This SHOULD NOT be implemented in the frontend / app")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Homepage link with eventual error codes in the "
            + "`error` query param. Take a look at "
            + "[`net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode`]"
            + "(https://github.com/APSfurizon/fz-backend/blob/master/application/"
            + "src/main/java/net/furizon/backend/feature/pretix/ordersworkflow/OrderWorkflowErrorCode.java)"
            + " for a full list of error codes with description", content = @Content)
    })
    @NotNull
    @GetMapping("order/{code}/{mainSecret}/open/{hmac}/")
    public RedirectView openOrderConfirmationLink(
            HttpServletRequest request,
            @AuthenticationPrincipal final FurizonUser user,
            @PathVariable("code") @NotNull final String code,
            @PathVariable("mainSecret") @NotNull final String mainSecret
    ) {
        return executor.execute(
            RegisterUserOrder.class,
            new RegisterUserOrder.Input(
                    user,
                    code,
                    mainSecret,
                    request,
                    pretixService
            )
        );
    }
}
