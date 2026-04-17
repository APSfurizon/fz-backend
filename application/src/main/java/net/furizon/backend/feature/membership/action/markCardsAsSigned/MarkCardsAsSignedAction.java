package net.furizon.backend.feature.membership.action.markCardsAsSigned;

import java.util.Collection;

public interface MarkCardsAsSignedAction {
    boolean invoke(Collection<Long> membershipCardIds);
}
