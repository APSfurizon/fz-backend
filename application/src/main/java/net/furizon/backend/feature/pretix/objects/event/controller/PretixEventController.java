package net.furizon.backend.feature.pretix.objects.event.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.dto.GetEventsResponse;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetAllEventsUseCase;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetSingleEventUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
