package net.furizon.backend.feature.organizers.finder;

import net.furizon.backend.feature.organizers.Organizer;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface OrganizersFinder {
    @NotNull
    PretixPaging<Organizer> getPagedOrganizers(int page);
}
