package net.furizon.backend.feature.pretix.event.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.event.PretixEvent;
import net.furizon.backend.feature.pretix.event.action.insert.InsertNewEventAction;
import net.furizon.backend.feature.pretix.event.action.update.UpdateEventAction;
import net.furizon.backend.feature.pretix.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.event.finder.pretix.PretixEventFinder;
import net.furizon.backend.feature.pretix.organizer.PretixOrganizer;
import net.furizon.backend.feature.pretix.organizer.finder.OrganizersFinder;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This use case should get all events from Pretix
 * Insert it to Database if not exist there (yet)
 * Returns the current event object or null if non was found
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReloadEventsUseCase implements UseCase<UseCaseInput, Optional<Event>> {
    private final OrganizersFinder organizersFinder;
    private final PretixEventFinder pretixEventFinder;
    private final EventFinder eventFinder;

    private final InsertNewEventAction insertEventAction;
    private final UpdateEventAction updateEventAction;

    private final PretixConfig pretixConfig;

    @Transactional
    @NotNull
    @Override
    public Optional<Event> executor(@NotNull UseCaseInput input) {
        AtomicReference<Event> currentEvent = new AtomicReference<>(null);
        PretixPagingUtil.fetchAll(
            organizersFinder::getPagedOrganizers,
            r -> {
                for (final PretixOrganizer organizer : r.getFirst()) {
                    PretixPagingUtil.fetchAll(
                        paging -> pretixEventFinder.getPagedEvents(organizer.getSlug(), paging),
                        result -> {
                            final List<PretixEvent> events = result.getFirst();
                            final Set<String> eventsName = events.stream()
                                    .map(it -> it.getName().values())
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toSet());

                            for (final PretixEvent event : events) {
                                Event dbEvent = eventFinder.findEventBySlug(
                                        Event.buildSlug(event.getSlug(), organizer.getSlug()));
                                boolean isCurrentEvent =  pretixConfig
                                        .getDefaultOrganizer()
                                        .equals(organizer.getSlug())
                                    && pretixConfig
                                        .getDefaultEvent()
                                        .equals(event.getSlug());


                                if (dbEvent == null) {
                                    //Create new event
                                    Event newEvent = Event.builder()
                                            .slug(event.getSlug(), organizer.getSlug())
                                            .publicUrl(event.getPublicUrl())
                                            .eventNames(eventsName)
                                            .isCurrent(isCurrentEvent)
                                            .dateTo(event.getDateTo()) // namings in db not the same as in pretix?
                                            .dateFrom(event.getDateFrom()) // is from is start?
                                            .build();

                                    insertEventAction.invoke(newEvent);

                                    // in case if we won't find an existed current event
                                    if (isCurrentEvent) {
                                        currentEvent.set(newEvent);
                                    }
                                } else {
                                    //Update existing event
                                    dbEvent.setPublicUrl(event.getPublicUrl());
                                    dbEvent.setDateFrom(event.getDateFrom());
                                    dbEvent.setDateTo(event.getDateTo());
                                    dbEvent.setEventNames(eventsName);
                                    dbEvent.setCurrent(isCurrentEvent);
                                    updateEventAction.invoke(dbEvent);
                                }

                                if (dbEvent != null && dbEvent.isCurrent()) {
                                    currentEvent.set(dbEvent);
                                }
                            }
                        }
                    );
                }
            }
        );

        if (currentEvent.get() == null) {
            log.warn("Could not find the current event");
        }

        return Optional.ofNullable(currentEvent.get());
    }
}