package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
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
    private final UseCaseExecutor executor;

    private final FrontendConfig config;

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
        if (user == null) {
            return new RedirectView(config.getLoginRedirectUrl(request.getRequestURL().toString()));
        }
        /*return executor.execute(

        );*/
        return null;
    }
}
