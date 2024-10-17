package net.furizon.backend.feature.organizers.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.organizers.Organizer;
import net.furizon.backend.feature.organizers.finder.OrganizersFinder;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restapi/v0/organizers")
@RequiredArgsConstructor
public class OrganizerController {
    private final OrganizersFinder finder;

    @GetMapping
    PretixPaging<Organizer> organizers() {
        return finder.getPagedOrganizers(1);
    }
}