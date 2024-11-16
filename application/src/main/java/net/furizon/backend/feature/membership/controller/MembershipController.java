package net.furizon.backend.feature.membership.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.ShouldUpdateInfoResponse;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;

    @PostMapping("/should-update-info")
    public ShouldUpdateInfoResponse logoutUser(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        boolean res = executor.execute(
                CheckIfUserShouldUpdateInfoUseCase.class,
                new CheckIfUserShouldUpdateInfoUseCase.Input(
                        user,
                        pretixInformation.getCurrentEvent().orElse(null)
                )
        );

        return res ? ShouldUpdateInfoResponse.YES : ShouldUpdateInfoResponse.NO;
    }
}
