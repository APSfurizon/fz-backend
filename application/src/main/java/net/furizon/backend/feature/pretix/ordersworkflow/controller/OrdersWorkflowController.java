package net.furizon.backend.feature.pretix.ordersworkflow.controller;

import lombok.RequiredArgsConstructor;
/*import net.furizon.backend.feature.authentication.dto.LogoutUserResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;*/
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders-workflow")
@RequiredArgsConstructor
public class OrdersWorkflowController {
    //private final UseCaseExecutor executor;

    /*@GetMapping("/generate-pretix-shop-link")
    public LinkResponse generatePretixShopLink(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(

        );
    }

    @GetMapping("/get-order-edit-link")
    public LogoutUserResponse logoutUser(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(

        );
    }*/

}
