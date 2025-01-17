package net.furizon.backend.feature.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.AddMembershipCardRequest;
import net.furizon.backend.feature.membership.dto.DeleteMembershipCardRequest;
import net.furizon.backend.feature.membership.dto.GetMembershipCardsResponse;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.dto.SetMembershipCardRegistrationStatusRequest;
import net.furizon.backend.feature.membership.dto.ShouldUpdateInfoResponse;
import net.furizon.backend.feature.membership.usecase.CheckIfUserShouldUpdateInfoUseCase;
import net.furizon.backend.feature.membership.usecase.CreateMembershipUseCase;
import net.furizon.backend.feature.membership.usecase.DeleteMembershipCardUseCase;
import net.furizon.backend.feature.membership.usecase.GetPersonalUserInformationUseCase;
import net.furizon.backend.feature.membership.usecase.LoadAllMembershipInfosUseCase;
import net.furizon.backend.feature.membership.usecase.MarkPersonalUserInformationAsUpdatedUseCase;
import net.furizon.backend.feature.membership.usecase.SetCardRegisterStatusUseCase;
import net.furizon.backend.feature.membership.usecase.UpdatePersonalUserInformationUseCase;
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

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    private final PretixInformation pretixInformation;

    private final UseCaseExecutor executor;

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
                        pretixInformation.getCurrentEvent()
                )
        );

        return res ? ShouldUpdateInfoResponse.YES : ShouldUpdateInfoResponse.NO;
    }

    @Operation(summary = "Update the personal user information of the user", description =
        "This method also resets the last event where the personal user information has "
        + "been updated to the current event. Returns `false` if the update has failed")
    @PostMapping("/update-personal-user-information")
    public boolean updateInfo(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final PersonalUserInformation personalUserInformation
    ) {
        return executor.execute(
                UpdatePersonalUserInformationUseCase.class,
                new UpdatePersonalUserInformationUseCase.Input(
                    user,
                    personalUserInformation,
                    pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Gets the personal information about the current logged in user")
    @GetMapping("/get-personal-user-information")
    public PersonalUserInformation getInfo(@AuthenticationPrincipal @NotNull final FurizonUser user) {
        return executor.execute(GetPersonalUserInformationUseCase.class, user);
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
                        pretixInformation.getCurrentEvent()
                )
        );
    }


    @Operation(summary = "Add a membership card to an user for the current event", description =
        "Adds a membership card object for the specified user in the current event's year. "
        + "This method should be executed ONLY by a backend admin!!")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_MEMBERSHIP_CARDS})
    @PostMapping("/add-card")
    public boolean addMembershipCard(
            @NotNull @Valid @RequestBody final AddMembershipCardRequest req,
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        executor.execute(
            CreateMembershipUseCase.class,
            new CreateMembershipUseCase.Input(
                req,
                user,
                pretixInformation.getCurrentEvent()
            )
        );
        return true;
    }

    @Operation(summary = "Deletes a membership card", description =
            "Deletes the specified membership card. Keep in mind that membership cards "
            + "with still active orders will be recreated automatically next time the backend "
            + "will fetch it from pretix, so the deletions of these kinds of cards is NOT possible "
            + "and will return a `MEMBERSHIP_CARD_ORDER_STILL_LINKED` error. It's NOT possible "
            + "as well the deletion of already registered cards, in this instance the error "
            + "will be `MEMBERSHIP_CARD_WAS_REGISTERED`")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_MEMBERSHIP_CARDS})
    @PostMapping("/delete-card")
    public boolean deleteMembershipCards(
            @NotNull @Valid @RequestBody final DeleteMembershipCardRequest req,
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        executor.execute(
                DeleteMembershipCardUseCase.class,
                req
        );
        return true;
    }

    @Operation(summary = "Gets a list of membership cards by year", description =
        "Returns a list of membership cards with the full (current) info of the respective user. "
        + "`year` param is optional, if it's not present or it's `< 0` we return the list of membership cards for "
        + "the current event. This method should be executed ONLY by a backend admin!!")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_MEMBERSHIP_CARDS})
    @GetMapping("/get-cards")
    public GetMembershipCardsResponse getMembershipCards(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestParam("year")
            final short year
    ) {
        return executor.execute(
            LoadAllMembershipInfosUseCase.class,
            new LoadAllMembershipInfosUseCase.Input(
                    year,
                    pretixInformation.getCurrentEvent()
            )
        );
    }

    @Operation(summary = "Sets the registration status of a membership card", description =
        "Membership cards have to be manually register to an external website for being legally valid."
        + "This endpoint should be used by ADMINS only and it's used to mark a membership card as registered "
        + "in the external website or not")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_MEMBERSHIP_CARDS})
    @PostMapping("/set-membership-card-registration-status")
    public boolean setMembershipCardRegistration(
            @NotNull @Valid @RequestBody final SetMembershipCardRegistrationStatusRequest req,
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                SetCardRegisterStatusUseCase.class,
                req
        );
    }
}
