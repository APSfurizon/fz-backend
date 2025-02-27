package net.furizon.backend.feature.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.templating.service.CustomTemplateService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetBadgesToPrintUseCase implements UseCase<GetBadgesToPrintUseCase.Input, String> {
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final BadgeFinder badgeFinder;
    @NotNull private final FursuitConfig fursuitConfig;
    @NotNull private final FursuitFinder fursuitFinder;
    //@NotNull private final GenerateBadgesAction generateBadgesAction;
    @NotNull private final CustomTemplateService customTemplateService;

    @Override
    public String executor(@NotNull Input input) {
        String badgeExample = customTemplateService.renderTemplate(
                "badge_normal.jte", Map.of("nickname", "person", "profileImageUrl", "https://fzbe.furizon.net/static/images/badges/6/787b15d5-90f5-45b1-88fa-1b33792c32c6.webp"));
        String badgeExample2 = customTemplateService.renderTemplate(
                "badge_normal.jte", Map.of("nickname", "person", "profileImageUrl", "https://fzbe.furizon.net/static/images/badges/6/787b15d5-90f5-45b1-88fa-1b33792c32c6.webp"));
        String output = customTemplateService.renderTemplate(
                "badge_output.jte", Map.of("renderedBadges", List.of(badgeExample, badgeExample2)));
        return output;
    }


    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
