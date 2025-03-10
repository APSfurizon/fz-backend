package net.furizon.backend.feature.nosecount.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.responses.AdminCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.FursuitCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.NoseCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.SponsorCountResponse;
import net.furizon.backend.feature.nosecount.usecase.LoadAdminCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadFursuitCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadNoseCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadSponsorCountUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counts")
@RequiredArgsConstructor
public class CountsController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;
    private final EventFinder eventFinder;

    @Operation(summary = "Gets the fursuit count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the fursuit count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only the fursuits brought to that event "
        + "with the `displayInNosecount` check will be returned by this method")
    @GetMapping("/fursuit")
    public FursuitCountResponse getFursuitCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }
        return executor.execute(
                LoadFursuitCountUseCase.class,
                eventId
        );
    }

    @Operation(summary = "Gets the sponsor count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the sponsor count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only users with a sponsorship level "
        + "> NONE will be displayed from this endpoint")
    @GetMapping("/sponsors")
    public SponsorCountResponse getSponsorCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }
        return executor.execute(
                LoadSponsorCountUseCase.class,
                eventId
        );
    }

    @Operation(summary = "Gets the nose count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the nose count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only users with the `showInNosecount` "
        + "tick set to true will be displayed from this endpoint")
    @GetMapping("/bopos")
    public NoseCountResponse getNosecount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        Event event;
        if (eventId == null) {
            event = pretixInformation.getCurrentEvent();
        } else {
            event = eventFinder.findEventById(eventId);
        }
        return executor.execute(
                LoadNoseCountUseCase.class,
                new LoadNoseCountUseCase.Input(
                        event,
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Gets the admin count", description =
        "This method DOES NOT returns only the admins coming to the current event, "
        + "but ALL users which are in a role noted as 'show in admin count'")
    @GetMapping("/admins")
    public AdminCountResponse getAdminCount() {
        return executor.execute(LoadAdminCountUseCase.class, 0);
    }
}
