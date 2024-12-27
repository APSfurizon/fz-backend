package net.furizon.backend.feature.room.action.confirmUserExchangeStatus;

public interface ConfirmUserExchangeStatusAction {
    boolean invoke(boolean isSourceUser, long exchangeId);
}
