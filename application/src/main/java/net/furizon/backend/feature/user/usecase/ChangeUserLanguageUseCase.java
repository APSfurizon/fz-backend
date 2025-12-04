package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.action.updateUserLanguage.UpdateUserLanguageAction;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.localization.TranslationUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeUserLanguageUseCase implements UseCase<ChangeUserLanguageUseCase.Input, Boolean> {
    @NotNull private final UpdateUserLanguageAction updateUserLanguageAction;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull ChangeUserLanguageUseCase.Input input) {
        Set<Locale> availableLanguages = translationService.getAvailableLanguages();
        Locale locale = TranslationUtil.parseLocale(input.apiInput.languageCode);
        if (!availableLanguages.contains(locale)) {
            throw new ApiException(translationService.error("user.language_not_supported"));
        }

        return updateUserLanguageAction.invoke(input.userId, locale);
    }

    public record Input(long userId, ApiInput apiInput) {}

    public record ApiInput(@NotNull String languageCode) {}
}
