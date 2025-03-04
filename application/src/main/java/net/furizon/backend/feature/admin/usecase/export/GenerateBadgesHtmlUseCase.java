package net.furizon.backend.feature.admin.usecase.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.GenerateBadgeRequest;
import net.furizon.backend.feature.badge.dto.PrintedBadgeLevel;
import net.furizon.backend.feature.badge.dto.UserBadgePrint;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.templating.service.CustomTemplateService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateBadgesHtmlUseCase implements UseCase<GenerateBadgesHtmlUseCase.Input, String> {
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final BadgeFinder badgeFinder;
    @NotNull private final CustomTemplateService customTemplateService;

    @Override
    public @NotNull String executor(@NotNull Input input) {
        String orderCodes = null;
        String orderSerials = null;
        String userIds = null;
        if (input.request != null) {
            orderCodes = input.request.getOrderCodes();
            orderSerials = input.request.getOrderSerials();
            userIds = input.request.getUserIds();
        }

        List<UserBadgePrint> badges = badgeFinder.getUserBadgesToPrint(
                input.pretixInformation.getCurrentEvent(),
                orderCodes,
                orderSerials,
                userIds
        );

        List<String> renderedBadges = new ArrayList<>(badges.size());

        for (UserBadgePrint badge : badges) {

            String imageUrl = badge.getImageUrl();
            imageUrl = imageUrl == null ? badgeConfig.getExport().getDefaultImageUrl() : imageUrl;
            String mimeType = badge.getImageMimeType();
            mimeType = mimeType == null ? "" : mimeType;

            PrintedBadgeLevel badgeLevel = PrintedBadgeLevel.NORMAL_BADGE;
            //In your convention you may want to reimplement this
            if (badge.getSponsorship() == Sponsorship.SPONSOR) {
                badgeLevel = PrintedBadgeLevel.NORMAL_SPONSOR;
            }
            if (badge.getSponsorship() == Sponsorship.SUPER_SPONSOR) {
                badgeLevel = PrintedBadgeLevel.SUPER_SPONSOR;
            }
            if (badge.getPermissions().contains(Permission.JUNIOR_STAFF)) {
                badgeLevel = PrintedBadgeLevel.JUNIOR_STAFF;
            }
            if (badge.getPermissions().contains(Permission.MAIN_STAFF)) {
                badgeLevel = PrintedBadgeLevel.MAIN_STAFF;
            }

            String html = customTemplateService.renderTemplate(
                badgeConfig.getExport().getUserBadgeJteFilename(),
                Map.of(
                    "userId", badge.getUserId(),
                    "serialNo", badge.getSerialNo(),
                    "orderCode", badge.getOrderCode(),
                    "fursonaName", badge.getFursonaName(),
                    "imageUrl", imageUrl,
                    "imageMimeType", mimeType,
                    "locales", badge.getLocales().getFirst(), //TODO support more flags
                    "sponsorship", badge.getSponsorship(),
                    "badgeLevel", badgeLevel
                )
            );
            renderedBadges.add(html);
        }

        return customTemplateService.renderTemplate(
            badgeConfig.getExport().getOutputWrapperBadgeJteFilename(),
            Map.of("renderedBadges", renderedBadges)
        );
    }


    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation,
            @Nullable GenerateBadgeRequest request
    ) {}
}
