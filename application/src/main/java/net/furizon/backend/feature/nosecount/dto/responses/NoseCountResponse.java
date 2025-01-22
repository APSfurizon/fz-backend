package net.furizon.backend.feature.nosecount.dto.responses;

import lombok.Data;
import net.furizon.backend.feature.nosecount.dto.NosecountHotel;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class NoseCountResponse {
    @NotNull private final List<NosecountHotel> hotels;

    @NotNull private final List<UserDisplayData> ticketOnlyFurs;
    @NotNull private final Map<LocalDate, List<UserDisplayData>> dailyFurs;
}
