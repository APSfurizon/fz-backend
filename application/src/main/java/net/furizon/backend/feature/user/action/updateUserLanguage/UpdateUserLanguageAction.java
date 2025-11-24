package net.furizon.backend.feature.user.action.updateUserLanguage;

import java.util.Locale;

public interface UpdateUserLanguageAction {
    boolean invoke(long userId, Locale userLocale);
}
