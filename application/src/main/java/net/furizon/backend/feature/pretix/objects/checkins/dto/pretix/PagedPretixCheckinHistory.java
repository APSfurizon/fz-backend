package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class PagedPretixCheckinHistory {
    @NotNull
    private final Long count;
    @Nullable
    private final Long next;
    @Nullable
    private final Long previous;

    @NotNull
    private final List<PretixCheckinHistory> results;
}
