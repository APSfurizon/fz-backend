package net.furizon.backend.feature.pretix.objects.checkins.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinHistory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class CheckinHistoryResponse {
    @NotNull
    private final Long count;
    @Nullable
    private final Long next;
    @Nullable
    private final Long previous;

    @NotNull
    private final List<CheckinHistory> results;
}
