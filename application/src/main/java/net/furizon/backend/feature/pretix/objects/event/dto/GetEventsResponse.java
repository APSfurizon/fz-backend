package net.furizon.backend.feature.pretix.objects.event.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class GetEventsResponse {
    @NotNull private final List<Event> events;
}
