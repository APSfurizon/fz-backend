package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.PretixEvent;
import net.furizon.backend.feature.pretix.objects.event.action.insert.InsertNewEventAction;
import net.furizon.backend.feature.pretix.objects.event.action.update.UpdateEventAction;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.event.finder.pretix.PretixEventFinder;
import net.furizon.backend.feature.pretix.objects.organizer.PretixOrganizer;
import net.furizon.backend.feature.pretix.objects.organizer.finder.OrganizersFinder;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This use case should get all events from Pretix
 * Insert it to Database if not exist there (yet) or update them
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
        List<PretixOrganizer> organizers = PretixPagingUtil.fetchAll(organizersFinder::getPagedOrganizers);
        for (final PretixOrganizer organizer : organizers) {
            PretixPagingUtil.forEachElement(
                paging -> pretixEventFinder.getPagedEvents(organizer.getSlug(), paging),
                result -> {
                    final PretixEvent event = result.getLeft();

                    Event dbEvent = eventFinder.findEventBySlug(
                            PretixGenericUtils.buildOrgEventSlug(event.getSlug(), organizer.getSlug()));
                    boolean isCurrentEvent = pretixConfig
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
                                .eventNames(event.getName())
                                .isCurrent(isCurrentEvent)
                                .dateTo(event.getDateTo())
                                .dateFrom(event.getDateFrom())
                                .isLive(event.isLive())
                                .testModeEnabled(event.isTestMode())
                                .isPublic(event.isPublic())
                                .build();

                        long dbId = insertEventAction.invoke(newEvent);
                        newEvent.setId(dbId);

                        // in case if we won't find an existed current event
                        if (isCurrentEvent) {
                            currentEvent.set(newEvent);
                        }
                    } else {
                        //Update existing event
                        dbEvent.setPublicUrl(event.getPublicUrl());
                        dbEvent.setDateFrom(event.getDateFrom());
                        dbEvent.setDateTo(event.getDateTo());
                        dbEvent.setEventNames(event.getName());
                        dbEvent.setCurrent(isCurrentEvent);
                        dbEvent.setLive(event.isLive());
                        dbEvent.setTestModeEnabled(event.isTestMode());
                        dbEvent.setPublic(event.isPublic());
                        updateEventAction.invoke(dbEvent);
                    }

                    if (dbEvent != null && dbEvent.isCurrent()) {
                        currentEvent.set(dbEvent);
                    }
                }
            );
        }

        if (currentEvent.get() == null) {
            log.warn("Unable to refresh currentEvent from pretix! Trying to fetch it from the DB");
            Event dbEvent = eventFinder.findEventBySlug(
                    PretixGenericUtils.buildOrgEventSlug(
                            pretixConfig.getDefaultEvent(),
                            pretixConfig.getDefaultOrganizer()
                    )
            );
            if (dbEvent == null) {
                log.error("Could not load the current event");
            } else {
                currentEvent.set(dbEvent);
            }
        }

        return Optional.ofNullable(currentEvent.get());
    }
}
