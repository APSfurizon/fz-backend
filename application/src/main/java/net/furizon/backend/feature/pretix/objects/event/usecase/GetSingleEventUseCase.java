package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSingleEventUseCase implements UseCase<Long, Event> {
    @NotNull private final EventFinder finder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Event executor(@NotNull Long eventId) {
        Event e = finder.findEventById(eventId);
        if (e == null) {
            throw new ApiException(translationService.error("common.event_not_found"),
                GeneralResponseCodes.EVENT_NOT_FOUND);
        }
        e.setPublicUrl("");
        return e;
    }
}
