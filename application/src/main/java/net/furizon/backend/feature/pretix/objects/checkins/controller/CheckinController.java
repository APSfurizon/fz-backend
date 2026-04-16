package net.furizon.backend.feature.pretix.objects.checkins.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CancelCheckinRequest;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinHistoryOrder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinSearchOrder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.RedeemCheckinRequest;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinHistoryResponse;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinResponse;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinSearchResponse;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.GetCheckinListResponse;
import net.furizon.backend.feature.pretix.objects.checkins.usecase.CancelCheckinUseCase;
import net.furizon.backend.feature.pretix.objects.checkins.usecase.FetchCheckinLogsUseCase;
import net.furizon.backend.feature.pretix.objects.checkins.usecase.FetchCheckinsUseCase;
import net.furizon.backend.feature.pretix.objects.checkins.usecase.GetCheckinListsUseCase;
import net.furizon.backend.feature.pretix.objects.checkins.usecase.RedeemCheckinUseCase;
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

import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {
    @NotNull
    private final UseCaseExecutor executor;
    @NotNull
    private final PretixInformation pretixService;

    @Operation(summary = "Retrieve the checkin lists from pretix", description =
        "Pretix has a thing called 'checkin lists'. Basically you can create "
        + "various lists per event and assign some items to these lists. Checkins "
        + "will in this way be item based and not oreder based. You can create a list "
        + "for tickets and another list for gadgets or subevents. With this endpoint "
        + "you can obtain a list of checkinLists enabled for this event. The checkin "
        + "application should first present operators with a choice of the various list and "
        + "then note down the selected checkin list id. It will be used in the other endpoints.")
    @GetMapping("/lists")
    @PermissionRequired(permissions = {Permission.CAN_PERFORM_CHECKINS})
    public GetCheckinListResponse getCheckinLists(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetCheckinListsUseCase.class,
                new GetCheckinListsUseCase.Input(
                        user,
                        pretixService.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Redeems a checking by the secret", description = """
        By providing a checkin secret and some checkin lists, this endpoint tries
        to redeem the checkin and returns various information regarding the owner of
        the ticket, which should be displayed on screen. In particular, this method returns:
        The checkin result in the `status`, and a `localizedErrorReason` and
        `optionalErrorMessage` if the status is not OK.
        Another important field is the `nonce`: The checkin application should note it down
        and use it for referring to this checkin call. Other notable fields are
        `lanyardType` and `portaBadgeType` which is a precomputed value of which
        kind of lanyard and portabadge should be given to the customer.
        Finally, the `gadgets` array: This contains a list of items which should be
        given to the customer, together with their quantities and a map of localized names.
        The boolean `shirt` means that the gadget is a shirt, so it must be displayed on screen
        together with the specified `shirtSize`. Checkin application should always display
        a button to print the normal badge, but the button for printing the fursuits badges
        should be hidden if `hasFursuitBadge` is equal to false""")
    @PostMapping("/redeem")
    @PermissionRequired(permissions = {Permission.CAN_PERFORM_CHECKINS})
    public CheckinResponse redeemCheckin(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final RedeemCheckinRequest req
    ) {
        return executor.execute(
                RedeemCheckinUseCase.class,
                new RedeemCheckinUseCase.Input(
                        req.getCheckinListIds(),
                        req.getSecret(),
                        pretixService.getCurrentEvent(),
                        user,
                        pretixService
                )
        );
    }

    @Operation(summary = "Cancels an already redeemed checkin", description = """
        This method is meant to be called if an operator has accidentally checked in someone
        and wants to amend. This call should be put under heavy confirmation modale.
        To cancel a checkin, you have to provide the nonce returned by the redeem endpoint.
        An optional explanation is allowed. Keep in mind that a checkin can be canceled
        only before 15 minutes have passed since when it was redeemed first.
        This method returns true on success. The result should be shown to the operator""")
    @PostMapping("/cancel")
    @PermissionRequired(permissions = {Permission.CAN_PERFORM_CHECKINS})
    public boolean cancelCheckin(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final CancelCheckinRequest req
    ) {
        return executor.execute(
                CancelCheckinUseCase.class,
                new CancelCheckinUseCase.Input(
                        req.getNonce(),
                        req.getCheckinListIds(),
                        req.getExplanation(),
                        pretixService.getCurrentEvent(),
                        user
                )
        );
    }


    @Operation(summary = "Search checkins in current event", description = """
        This method returns impaginated results. Use next and prev to understand the list
        boundaries. Inside each single result you will get if the checkin has already been
        redeemed, and the secret code to redeem it with the normal endpoint.
        The calls to this method are kinda heavy, so please keep the results in cache and
        offer the user an extra button to refresh the data""")
    @GetMapping("/search")
    @PermissionRequired(permissions = {Permission.CAN_PERFORM_CHECKINS})
    public CheckinSearchResponse searchCheckin(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @Valid @Nullable @RequestParam final String query,
        @Valid @Nullable @RequestParam final Long checkinListId,
        @Valid @Nullable @RequestParam final Boolean hasCheckedIn,
        @Valid @Nullable @RequestParam final CheckinSearchOrder orderBy,
        @Valid @Nullable @RequestParam final Integer page
    ) {
        return executor.execute(
                FetchCheckinsUseCase.class,
                new FetchCheckinsUseCase.Input(
                        query,
                        checkinListId,
                        hasCheckedIn,
                        orderBy,
                        page,
                        pretixService.getCurrentEvent(),
                        user,
                        pretixService
                )
        );
    }

    @Operation(summary = "Retrieves the checkin logs for the current event", description = """
        This method returns impaginated results. Use next and prev to understand the list
        boundaries. For each result, you can have a boolean to say if the checkin was successful
        or not. If not, you have the extra messages like in the redeem endpoint""")
    @GetMapping("/logs")
    @PermissionRequired(permissions = {Permission.CAN_PERFORM_CHECKINS})
    public CheckinHistoryResponse getCheckinLogs(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @Nullable @RequestParam final OffsetDateTime createdSince,
            @Valid @Nullable @RequestParam final OffsetDateTime createdBefore,
            @Valid @Nullable @RequestParam final OffsetDateTime datetimeSince,
            @Valid @Nullable @RequestParam final OffsetDateTime datetimeBefore,
            @Valid @Nullable @RequestParam final Boolean successful,
            @Valid @Nullable @RequestParam final Long checkinListId,
            @Valid @Nullable @RequestParam final CheckinType type,
            @Valid @Nullable @RequestParam final Boolean autoCheckedIn,
            @Valid @Nullable @RequestParam final CheckinHistoryOrder order,
            @Valid @Nullable @RequestParam final Integer page
    ) {
        return executor.execute(
                FetchCheckinLogsUseCase.class,
                new FetchCheckinLogsUseCase.Input(
                        createdSince,
                        createdBefore,
                        datetimeSince,
                        datetimeBefore,
                        successful,
                        checkinListId,
                        type,
                        autoCheckedIn,
                        order,
                        page,
                        pretixService.getCurrentEvent(),
                        user
                )
        );
    }

}
