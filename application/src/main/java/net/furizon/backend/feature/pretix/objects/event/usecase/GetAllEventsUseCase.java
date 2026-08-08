package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.dto.GetEventsResponse;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllEventsUseCase implements UseCase<GetAllEventsUseCase.Input, GetEventsResponse> {
    @NotNull private final EventFinder finder;

    @Override
    public @NotNull GetEventsResponse executor(@NotNull Input input) {
        List<Event> events;
        if (input.withAttendees) {
            events = finder.getEventsWithAttendees();
            if (input.currentEvent != null) {
                long currentEventId = input.currentEvent.getId();
                if (events.stream().map(Event::getId).noneMatch(id -> id == currentEventId)) {
                    List<Event> l = new ArrayList<>(events.size() + 1);
                    l.add(input.currentEvent);
                    l.addAll(events);
                    events = l;
                }
            }
        } else {
            events = finder.getAllEvents();
        }
        events = events.stream().filter(Event::canBeShownToPublic).toList();
        events.forEach(event -> event.setPublicUrl(""));
        return new GetEventsResponse(events);
    }

    public record Input(
            boolean withAttendees,
            @Nullable Event currentEvent
    ) {}
}
