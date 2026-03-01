package net.furizon.backend.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.usecase.SearchUserInEventUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByFursuitIdUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByMembershipDbIdUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByMembershipNoUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByOrderCodeUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByOrderSerialUseCase;
import net.furizon.backend.feature.user.usecase.retrival.GetUsersByUserIdUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/search")
@RequiredArgsConstructor
public class SearchUserController {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @Operation(summary = "Search an user who attends the current event", description =
            "Using the query parameter `fursona-name` you can obtain a list of users who attend the current event. "
                    + "This method is intended for search&select purpose (eg: while inviting someone to your own room) "
                    + "and it returns a list of users with their id, full fursona name and propic url "
                    + "(it can be null!). This method purposely ignores the `show_in_nosecount` flag iff the name "
                    + "fully matches except for one single character. Results are ordered by match and alphabetically. "
                    + "Using the optional `filter-not-in-room` parameter you can filter out people who already "
                    + "own a room or are in a room. Using the optional `filter-paid` you can filter out only people "
                    + "with orders marked as paid. With `filter-no-membership-card` you can filter only people who "
                    + "don't have a membership card for the current event; specifying "
                    + "`filter-no-membership-card-for-year` will let you chose for which year. With `filterBanStatus` "
                    + "you can filter out people who are banned/not by the admins; by default everyone is returned. "
                    + "With `filterWithoutRole` you can filter out people who have the specified role, "
                    + "by the role internal name")
    @GetMapping("/current-event")
    public SearchUsersResponse searchByNameInCurrentEvent(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 2)
            @RequestParam("name")
            final String inputQuery,
            @Valid
            @Nullable
            @RequestParam("is-admin-search")
            final Boolean isAdminSearch,
            @Valid
            @Nullable
            @RequestParam("filter-not-in-room")
            final Boolean filterNotInRoom,
            @Valid
            @Nullable
            @RequestParam("filter-paid")
            final Boolean filterPaid,
            @Valid
            @Nullable
            @RequestParam("filter-not-made-an-order")
            final Boolean filterNotMadeAnOrder,
            @Valid
            @Nullable
            @RequestParam("filter-no-membership-card-for-year")
            final Short filterNoMembershipCardForYear,
            @Valid
            @Nullable
            @RequestParam("filter-ban-status")
            final Boolean filterBanStatus,
            @Valid
            @Nullable
            @RequestParam("filter-without-role")
            final String filterWithoutRole
    ) {
        return executor.execute(
                SearchUserInEventUseCase.class,
                new SearchUserInEventUseCase.Input(
                        user,
                        inputQuery,
                        isAdminSearch == null ? false : isAdminSearch,
                        pretixInformation,
                        filterNotInRoom == null ? false : filterNotInRoom,
                        filterPaid == null ? false : filterPaid,
                        filterNotMadeAnOrder == null ? false : filterNotMadeAnOrder,
                        filterNoMembershipCardForYear,
                        filterBanStatus,
                        filterWithoutRole
                )
        );
    }

    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @Operation(summary = "Search users by their orderCode in currentEvent context")
    @GetMapping("/current-event/by-order-code")
    public SearchUsersResponse searchByOrderCodeInCurrentEvent(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("orders")
            final String[] orderCodes
    ) {
        for (String s : orderCodes) {
            if (s == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByOrderCodeUseCase.class,
                new GetUsersByOrderCodeUseCase.Input(
                        orderCodes,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @Operation(summary = "Search users by their orderSerial in currentEvent context")
    @GetMapping("/current-event/by-order-serial")
    public SearchUsersResponse searchByOrderSerialInCurrentEvent(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("orders")
            final Long[] orderSerials
    ) {
        for (Long l : orderSerials) {
            if (l == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByOrderSerialUseCase.class,
                new GetUsersByOrderSerialUseCase.Input(
                        orderSerials,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    // This leaks userId -> fursona name/propic, but we really don't care
    @Operation(summary = "Search users by their userId")
    @GetMapping("/by-user-id")
    public SearchUsersResponse searchByUsersId(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("id")
            final Long[] userIds
    ) {
        for (Long l : userIds) {
            if (l == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByUserIdUseCase.class,
                new GetUsersByUserIdUseCase.Input(
                        userIds,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @Operation(summary = "Search users by the membership card database ID")
    @GetMapping("/by-membership-dbid")
    public SearchUsersResponse searchByMembershipCardDbId(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("id")
            final Long[] ids
    ) {
        for (Long l : ids) {
            if (l == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByMembershipDbIdUseCase.class,
                new GetUsersByMembershipDbIdUseCase.Input(
                        ids,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @Operation(summary = "Search users by the membership card number")
    @GetMapping("/by-membership-no")
    public SearchUsersResponse searchByMembershipCardNo(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("no")
            final String[] ids
    ) {
        for (String s : ids) {
            if (s == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByMembershipNoUseCase.class,
                new GetUsersByMembershipNoUseCase.Input(
                        ids,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Search users by a fursuit id", description =
        "This effectively returns the owner of a fursuit. "
        + "If the requester is not admin and the fursuit owner is not public "
        + "no results will be returned for that said fursuit")
    @GetMapping("/by-fursuit-id")
    public SearchUsersResponse searchByFursuitsId(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("id")
            final Long[] fursuitIds
    ) {
        for (Long l : fursuitIds) {
            if (l == null) {
                return null;
            }
        }
        return executor.execute(
                GetUsersByFursuitIdUseCase.class,
                new GetUsersByFursuitIdUseCase.Input(
                        fursuitIds,
                        pretixInformation.getCurrentEvent(),
                        user
                )
        );
    }
}
