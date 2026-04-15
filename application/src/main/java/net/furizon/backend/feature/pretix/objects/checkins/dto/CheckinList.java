package net.furizon.backend.feature.pretix.objects.checkins.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CheckinList {
    private final long id;
    @NotNull private final String name;
    private final long total;
    private final long checkedIn;
}
