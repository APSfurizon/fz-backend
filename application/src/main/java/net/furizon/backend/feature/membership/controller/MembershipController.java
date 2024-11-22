package net.furizon.backend.feature.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.AddMembershipCardRequest;
import net.furizon.backend.feature.membership.dto.ShouldUpdateInfoResponse;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.feature.membership.usecase.CreateMembershipUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @Operation(summary = "Check if an user has updated his membership card within the last event", description =
        "Every event the user should be prompted to double check if his personal information are still up to date, "
        + "together with a link to the page to change them. The text should also warn the user that his information "
        + "are used for health policy in furizon, so he should be sure they are correct.")
    @GetMapping("/should-update-info")
    public ShouldUpdateInfoResponse shouldUpdateInfo(
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


    @Operation(summary = "Add a membership card to an user for the current event", description =
        "Adds a membership card object for the specified user in the current event's year. "
        + "This method should be executed ONLY by a backend admin!!")
    @PostMapping("/add-membership-card")
    public boolean addMembershipCard(
            @Valid @RequestBody final AddMembershipCardRequest req,
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        //TODO [ADMIN_CHECK]
        executor.execute(
            CreateMembershipUseCase.class,
            new CreateMembershipUseCase.Input(
                req,
                user,
                pretixInformation.getCurrentEvent().orElse(null)
            )
        );
        return true;
    }
}
