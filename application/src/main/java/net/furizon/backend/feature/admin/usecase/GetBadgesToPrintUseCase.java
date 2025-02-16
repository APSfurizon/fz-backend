package net.furizon.backend.feature.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.infrastructure.admin.action.GenerateBadgesAction;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.documents.service.DocumentsService;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.templating.service.CustomTemplateService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetBadgesToPrintUseCase implements UseCase<GetBadgesToPrintUseCase.Input, Byte[]> {
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final BadgeFinder badgeFinder;
    @NotNull private final FursuitConfig fursuitConfig;
    @NotNull private final FursuitFinder fursuitFinder;
    //@NotNull private final GenerateBadgesAction generateBadgesAction;
    @NotNull private final CustomTemplateService customTemplateService;
    @NotNull private final DocumentsService documentsService;

    @Override
    public @NotNull Byte @NotNull [] executor(@NotNull Input input) {
        String text = customTemplateService.renderTemplate("badge_example.jte", Map.of("dato", "sesso"));
        try {
            return documentsService.convertHtmlToPdf(text);
        } catch (Throwable e) {
            return new Byte[] {};
        }
    }


    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
