package net.furizon.backend.feature.pretix.organizer.finder;

import net.furizon.backend.feature.pretix.organizer.PretixOrganizer;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface OrganizersFinder {
    @NotNull
    PretixPaging<PretixOrganizer> getPagedOrganizers(int page);
}
