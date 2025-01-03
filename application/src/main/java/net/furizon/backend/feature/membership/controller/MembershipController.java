package net.furizon.backend.feature.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.AddMembershipCardRequest;
import net.furizon.backend.feature.membership.dto.GetMembershipCardsResponse;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.dto.SetMembershipCardRegistrationStatusRequest;
import net.furizon.backend.feature.membership.dto.ShouldUpdateInfoResponse;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.feature.membership.usecase.CreateMembershipUseCase;
import net.furizon.backend.feature.membership.usecase.LoadAllMembershipInfosUseCase;
import net.furizon.backend.feature.membership.usecase.MarkPersonalUserInformationAsUpdatedUseCase;
import net.furizon.backend.feature.membership.usecase.SetCardRegisterStatusUseCase;
import net.furizon.backend.feature.membership.usecase.UpdatePersonalUserInformationUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @NotNull
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
                        getEvent()
                )
        );

        return res ? ShouldUpdateInfoResponse.YES : ShouldUpdateInfoResponse.NO;
    }

    @Operation(summary = "Update the personal user information of the user", description =
        "This method also resets the last event where the personal user information has "
        + "been updated to the current event. Returns `false` if the update has failed")
    @PostMapping("/update-persona-user-information")
    public boolean updateInfo(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final PersonalUserInformation personalUserInformation
    ) {
        return executor.execute(
                UpdatePersonalUserInformationUseCase.class,
                new UpdatePersonalUserInformationUseCase.Input(
                    user,
                    personalUserInformation,
                    getEvent()
                )
        );
    }

    @Operation(summary = "Resets the last information event updated", description =
        "Since it's critical to keep the personal user information updated for both "
        + "insurance and legal reasons, each different event we prompt the user to update "
        + "their information, or confirm that everything's still ok. This method is used to confirm "
        + "that the information are ok, and resets the last event where the personal user information "
        + "has been updated to the current event")
    @PostMapping("/mark-persona-user-information-as-updated")
    public boolean markInfoAsUpdated(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                MarkPersonalUserInformationAsUpdatedUseCase.class,
                new MarkPersonalUserInformationAsUpdatedUseCase.Input(
                        user,
                        getEvent()
                )
        );
    }


    @Operation(summary = "Add a membership card to an user for the current event", description =
        "Adds a membership card object for the specified user in the current event's year. "
        + "This method should be executed ONLY by a backend admin!!")
    @PostMapping("/add-card")
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
                getEvent()
            )
        );
        return true;
    }

    @Operation(summary = "Gets a list of membership cards by year", description =
        "Returns a list of membership cards with the full (current) info of the respective user. "
        + "`year` param is optional, if it's not present or it's `< 0` we return the list of membership cards for "
        + "the current event. This method should be executed ONLY by a backend admin!!")
    @GetMapping("/get-cards")
    public GetMembershipCardsResponse getMembershipCards(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @RequestParam("year")
            final short year
    ) {
        //TODO [ADMIN_CHECK]
        return executor.execute(
            LoadAllMembershipInfosUseCase.class,
            year
        );
    }

    @Operation(summary = "Sets the registration status of a membership card", description =
        "Membership cards have to be manually register to an external website for being legally valid."
        + "This endpoint should be used by ADMINS only and it's used to mark a membership card as registered "
        + "in the external website or not")
    @PostMapping("/set-membership-card-registration-status")
    public boolean setMembershipCardRegistration(
            @Valid @RequestBody final SetMembershipCardRegistrationStatusRequest req,
            @AuthenticationPrincipal@NotNull final FurizonUser user
    ) {
        //TODO [ADMIN_CHECK]
        return executor.execute(
                SetCardRegisterStatusUseCase.class,
                req
        );
    }

    //This should not be needed
    /*@Operation(summary = "Exports the membership cards for the given year in CSV", description =
        "If no year is specified or `year < 0`, the list of the current event is returned")
    public void exportMembershipCardList(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @RequestParam("year")
            final short year
    ) {
        //TODO [ADMIN_CHECK]
        //TODO
    }*/

    @Nullable
    private Event getEvent() {
        return pretixInformation.getCurrentEvent().orElse(null);
    }
}
