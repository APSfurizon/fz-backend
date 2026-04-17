package net.furizon.backend.feature.pretix.objects.checkins.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinList;

import java.util.List;

@Data
public class GetCheckinListResponse {
    public final List<CheckinList> checkinLists;
}
