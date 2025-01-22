package net.furizon.backend.feature.nosecount.finder;

import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.nosecount.dto.JooqNosecountObj;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CountsFinder {

    @NotNull List<FursuitDisplayData> getFursuits(long eventId);

    @NotNull List<UserDisplayData> getSponsors(long eventId);

    @NotNull List<JooqNosecountObj> getNosecount(long eventId);
}
