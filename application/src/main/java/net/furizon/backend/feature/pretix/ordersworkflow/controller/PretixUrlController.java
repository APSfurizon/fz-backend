package net.furizon.backend.feature.pretix.ordersworkflow.controller;

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
