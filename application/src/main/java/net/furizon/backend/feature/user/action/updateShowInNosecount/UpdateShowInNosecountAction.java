package net.furizon.backend.feature.user.action.updateShowInNosecount;

public interface UpdateShowInNosecountAction {
    boolean invoke(long userId, boolean showInNosecount);
}
