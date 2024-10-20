package net.furizon.backend.feature.event.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.event.Event;
import net.furizon.backend.feature.event.action.insert.InsertNewEventAction;
import net.furizon.backend.feature.event.finder.EventFinder;
import net.furizon.backend.feature.event.finder.pretix.PretixEventFinder;
import net.furizon.backend.feature.organizer.finder.OrganizersFinder;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
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
    // TODO -> Think about cache version for organizers?
    private final OrganizersFinder organizersFinder;

    private final PretixEventFinder pretixEventFinder;

    private final EventFinder eventFinder;

    private final PretixConfig pretixConfig;

    private final InsertNewEventAction insertEventAction;

    @Transactional
    @NotNull
    @Override
    public Optional<Event> executor(@NotNull UseCaseInput input) {
        final var organizers = organizersFinder.getPagedOrganizers(PretixPaging.DEFAULT_PAGE).getResults();
        if (organizers == null) {
            throw new ApiException("Could not get organizers from pretix");
        }

        AtomicReference<Event> currentEvent = new AtomicReference<>(null);
        for (final var organizer : organizers) {
            PretixPagingUtil.fetchAll(
                paging -> pretixEventFinder.getPagedEvents(organizer.getSlug(), paging),
                result -> {
                    final var events = result.getFirst();
                    final var eventsName = events.stream().map(it -> it.getName().values())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

                    for (final var event : events) {
                        final var databaseEvent = eventFinder.findEventBySlug(event.getSlug());
                        if (databaseEvent == null) {
                            final boolean isCurrentEvent = pretixConfig
                                .getDefaultOrganizer()
                                .equals(organizer.getSlug()) && pretixConfig
                                .getDefaultEvent()
                                .equals(event.getSlug());

                            final var newEvent = Event.builder()
                                .slug(event.getSlug())
                                .publicUrl(event.getPublicUrl())
                                .eventNames(eventsName)
                                .isCurrent(isCurrentEvent)
                                .dateEnd(event.getDateFrom()) // huh? namings in db not the same as in pretix?
                                .dateFrom(event.getDateTo()) // is from is start?
                                .build();

                            insertEventAction.invoke(newEvent);

                            // in case if we won't find an existed current event
                            if (isCurrentEvent) {
                                currentEvent.set(newEvent);
                            }
                        }

                        if (databaseEvent != null && Boolean.TRUE.equals(databaseEvent.getIsCurrent())) {
                            currentEvent.set(databaseEvent);
                        }
                    }
                }
            );
        }

        if (currentEvent.get() == null) {
            log.warn("Could not find the current event");
        }

        return Optional.ofNullable(currentEvent.get());
    }
}
