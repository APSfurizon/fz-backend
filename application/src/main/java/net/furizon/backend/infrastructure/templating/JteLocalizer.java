package net.furizon.backend.infrastructure.templating;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.localization.TranslationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JteLocalizer implements gg.jte.support.LocalizationSupport {
    @NotNull
    private final TranslationService translationService;

    @Override
    public String lookup(String s) {
        return translationService.email(s, new Object[] {});
    }
}
