package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinList;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixCheckinList {
    @NotNull
    private final Long id;
    @NotNull
    private final String name;

    @NotNull
    @JsonProperty("checkin_count")
    private final Long checkinCount;

    @NotNull
    @JsonProperty("position_count")
    private final Long positionCount;

    public @NotNull CheckinList toCheckinList() {
        return new CheckinList(id, name, positionCount, checkinCount);
    }
}
