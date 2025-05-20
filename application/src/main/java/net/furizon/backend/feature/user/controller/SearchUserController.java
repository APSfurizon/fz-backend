package net.furizon.backend.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.usecase.GetSelectedUsersByIdsUseCase;
import net.furizon.backend.feature.user.usecase.SearchUserInEventUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
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

    @GetMapping("/current-event/by-id")
    public SearchUsersResponse getUsersInCurrentEventById(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 1)
            @RequestParam("id")
            final String[] userIds
    ) {
        return executor.execute(
                GetSelectedUsersByIdsUseCase.class,
                new GetSelectedUsersByIdsUseCase.Input(
                        userIds,
                        pretixInformation.getCurrentEvent()
                )
        );
    }
}
