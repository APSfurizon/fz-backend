package net.furizon.backend.feature.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.ShouldUpdateInfoResponse;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;

    @Operation(summary = "Check if an user has updated his membership card within the last event", description =
        "Every event the user should be prompted to double check if his personal information are still up to date, "
        + "together with a link to the page to change them. The text should also warn the user that his information "
        + "are used for health policy in furizon, so he should be sure they are correct.")
    @GetMapping("/should-update-info")
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
