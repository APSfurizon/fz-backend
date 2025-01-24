package net.furizon.backend.feature.fursuits.finder;

import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FursuitFinder {
    @NotNull List<FursuitData> getFursuitsOfUser(long userId, @Nullable Event event);

    @Nullable FursuitData getFursuit(long fursuitId, @Nullable Event event);

    int countFursuitsOfUser(long userId);

    int countFursuitOfUserToEvent(long userId, @NotNull Order order);

    @Nullable Long getFursuitOwner(long fursuitId);

    boolean isFursuitBroughtToEvent(long fursuitId, @NotNull Order order);
}
