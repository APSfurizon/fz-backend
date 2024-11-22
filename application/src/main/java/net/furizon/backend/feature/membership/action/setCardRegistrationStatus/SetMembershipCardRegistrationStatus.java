package net.furizon.backend.feature.membership.action.setCardRegistrationStatus;

public interface SetMembershipCardRegistrationStatus {
    void invoke(long membershipCardId, boolean status);
}
