package net.furizon.backend.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.UserSession;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayDataResponse;
import net.furizon.backend.feature.user.usecase.GetUserDisplayDataUseCase;
import net.furizon.backend.feature.user.usecase.GetUserSessionsUseCase;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @GetMapping("/me")
    public FurizonUser getMe(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return user;
    }

    @GetMapping("/me/display")
    public Optional<UserDisplayDataResponse> getMeDisplay(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetUserDisplayDataUseCase.class,
                new GetUserDisplayDataUseCase.Input(
                    user.getUserId(),
                    pretixInformation.getCurrentEvent()
                )
        );
    }

    @GetMapping("/me/sessions")
    public List<UserSession> getMeSessions(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
            GetUserSessionsUseCase.class,
            user.getUserId()
        );
    }

    @Operation(summary = "Search an user who attends the current event", description =
        "Using the query parameter `fursona-name` you can obtain a list of users who attend the current event. "
        + "This method is intended for search&select purpose (eg: while inviting someone to your own room) and "
        + "it returns a list of users with their id, full fursona name and propic url (it can be null!). "
        + "This method purposely ignores the `show_in_nosecount` flag iff the name fully matches except "
        + "for one single character. Results are ordered by match and alphabetically. "
        + "Using the optional `filter-room` parameter you can filter out people who already "
        + "own a room or are in a room")
    @GetMapping("/search-in-current-event")
    public SearchUsersResponse searchByFursonaNameInCurrentEvent(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid
            @jakarta.validation.constraints.NotNull
            @Size(min = 2)
            @RequestParam("fursona-name")
            final String fursonaName,
            @Valid
            @Nullable
            @RequestParam("filter-room")
            final Boolean filterRoom
    ) {
        return executor.execute(
                SearchUserInEventUseCase.class,
                new SearchUserInEventUseCase.Input(
                        fursonaName,
                        pretixInformation,
                        filterRoom == null ? false : filterRoom
                )
        );
    }
}
