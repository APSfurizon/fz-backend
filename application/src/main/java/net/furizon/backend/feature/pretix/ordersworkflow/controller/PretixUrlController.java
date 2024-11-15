package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.LogoutUserResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
There are some GET requests initialized by the user which normally would go to pretix,
but we catch them from the reverse proxy. This class is responsible for handling them
 */
@RestController
@RequestMapping("${pretix.path}/${pretix.default-organizer}/${pretix.default-event}")
@RequiredArgsConstructor
public class PretixUrlController {
    private final UseCaseExecutor executor;

    //secret2 is currently unused: TODO check if we should check it as well
    @GetMapping("/order/{code}/{mainSecret}/open/{secret2}/")
    public LogoutUserResponse openOrderConfirmationLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        //TODO
        /*return executor.execute(

        );*/
        return null;
    }
}
