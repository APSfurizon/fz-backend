package net.furizon.backend.feature.membership.action.shiftEnumerationOfCards;

public interface ShiftEnumerationOfMembershipCardsAction {
    int invoke(short year, int startingFromIdInYear, int shiftAmount);
}
