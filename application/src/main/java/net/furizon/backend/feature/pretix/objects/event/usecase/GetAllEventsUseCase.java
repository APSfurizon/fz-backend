package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.dto.GetEventsResponse;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllEventsUseCase implements UseCase<Integer, GetEventsResponse> {
    @NotNull private final EventFinder finder;

    @Override
    public @NotNull GetEventsResponse executor(@NotNull Integer input) {
        List<Event> events = finder.getAllEvents();
        events = events.stream().filter(Event::canBeShownToPublic).toList();
        events.forEach(event -> event.setPublicUrl(""));
        return new GetEventsResponse(events);
    }
}
