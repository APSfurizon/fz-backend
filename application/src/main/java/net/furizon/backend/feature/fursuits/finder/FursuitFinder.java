package net.furizon.backend.feature.fursuits.finder;

import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FursuitFinder {
    @NotNull List<FursuitDisplayData> getFursuitsOfUser(long userId, Event event);

    @Nullable FursuitDisplayData getFursuit(long fursuitId, Event event);
}
