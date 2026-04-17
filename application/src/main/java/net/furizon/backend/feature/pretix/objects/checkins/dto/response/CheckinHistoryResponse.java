package net.furizon.backend.feature.pretix.objects.checkins.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinHistory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class CheckinHistoryResponse {
    private final int count;
    @Nullable private final Integer next;
    @Nullable private final Integer previous;

    @NotNull
    private final List<CheckinHistory> results;
}
