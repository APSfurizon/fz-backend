package net.furizon.backend.feature.pretix.objects.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.dto.GetEventsResponse;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetAllEventsUseCase;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetAttendedEvents;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetSingleEventUseCase;
import net.furizon.backend.feature.pretix.objects.event.dto.SponsorshipNamesResponse;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetSponsorshipNamesUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class PretixEventController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;

    @GetMapping("/")
    public GetEventsResponse getEvents() {
        return executor.execute(GetAllEventsUseCase.class, 0);
    }

    @GetMapping("/current")
    public Event getCurrent() {
        Event e = pretixInformation.getCurrentEvent();
        e.setPublicUrl("");
        return e;
    }

    @GetMapping("/{eventId}")
    public Event getSingleEvent(@PathVariable("eventId") @NotNull final Long eventId) {
        return executor.execute(GetSingleEventUseCase.class, eventId);
    }

    @GetMapping("/get-sponsorship-names/{eventId}")
    public SponsorshipNamesResponse getSponsorshipNames(@PathVariable("eventId") @NotNull final Long eventId) {
        return executor.execute(
                GetSponsorshipNamesUseCase.class,
                new GetSponsorshipNamesUseCase.Input(eventId, pretixInformation)
        );
    }

    @Operation(summary = "Get the list of events attended by specified user", description =
        "By default current logged in user is selected. An user with permission "
        + "`CAN_MANAGE_USER_PUBLIC_INFO` can load this list of another user, by specifying its"
        + "id in the `userId` get param")
    @GetMapping("/attended")
    public GetEventsResponse getAttendedEvents(
            @RequestParam @Nullable @Valid @Positive final Long userId,
            @AuthenticationPrincipal @Valid @Nullable final FurizonUser user
    ) {
        if (user == null) {
            log.warn("/events/attended called without being logged in. Returning empty list");
            return new GetEventsResponse(Collections.emptyList());
        }
        return executor.execute(
                GetAttendedEvents.class,
                new GetAttendedEvents.Input(
                        userId,
                        user
                )
        );
    }
}
