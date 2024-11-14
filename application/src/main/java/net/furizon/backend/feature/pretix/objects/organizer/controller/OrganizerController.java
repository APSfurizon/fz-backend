package net.furizon.backend.feature.pretix.objects.organizer.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.organizer.PretixOrganizer;
import net.furizon.backend.feature.pretix.objects.organizer.finder.OrganizersFinder;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/organizers")
@RequiredArgsConstructor
public class OrganizerController {
    private final OrganizersFinder finder;

    @GetMapping
    public PretixPaging<PretixOrganizer> organizers() {
        return finder.getPagedOrganizers(1);
    }
}
