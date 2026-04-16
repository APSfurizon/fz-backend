package net.furizon.backend.feature.pretix.objects.checkins.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinSearchResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class CheckinSearchResponse {
    private final int count;
    private final int next;
    private final int previous;

    @NotNull
    private final List<CheckinSearchResult> results;
}
