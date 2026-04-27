package net.furizon.backend.feature.membership.action.markCardsAsSent;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface MarkCardsAsSentAction {
    boolean invoke(@NotNull Collection<Long> cardIds);
}
