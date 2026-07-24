package net.furizon.backend.feature.membership.action.setCardRegistrationStatus;

public interface SetMembershipCardRegistrationStatus {
    boolean invoke(long membershipCardId, boolean status);
}
