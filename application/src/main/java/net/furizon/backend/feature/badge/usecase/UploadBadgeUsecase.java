package net.furizon.backend.feature.badge.usecase;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.setBadge.SetFursuitBadgeAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.feature.badge.validator.UploadUserBadgeValidator;
import net.furizon.backend.feature.user.action.setBadge.SetUserBadgeAction;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.media.SimpleImageMetadata;
import net.furizon.backend.infrastructure.media.action.StoreMediaOnDiskAction;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadBadgeUsecase implements UseCase<UploadBadgeUsecase.Input, MediaResponse> {

    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks checks;

    @NotNull private final UploadUserBadgeValidator validator;

    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;
    @NotNull private final StoreMediaOnDiskAction storeMediaOnDiskAction;
    @NotNull private final SetFursuitBadgeAction setFursuitBadgeAction;
    @NotNull private final SetUserBadgeAction setUserBadgeAction;

    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final BadgeFinder badgeFinder;

    @NotNull private final FursuitChecks fursuitChecks;

    @NotNull private final TranslationService translationService;

    @Override
    @Transactional
    public @NotNull MediaResponse executor(@NotNull Input input) {
        try {
            long userId = input.user.getUserId();
            boolean isAdmin = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO);
            if (input.checkTimeframe) {
                checks.assertTimeframeForEventNotPassedAllowAdmin(
                        badgeConfig.getEditingDeadline(),
                        input.event,
                        input.fursuitId, //THIS IS RIGHT!!
                        userId,
                        null,
                        isAdmin
                );
            }
            if (input.type == BadgeType.BADGE_FURSUIT) {
                Objects.requireNonNull(input.fursuitId);
                fursuitChecks.assertUserHasPermissionOnFursuit(userId, input.fursuitId, isAdmin);
            }
            log.info("[BADGE] User {} is uploading a {} badge: FursuitVal = {}",
                    userId, input.type, input.fursuitId);

            SimpleImageMetadata imageMetadata = validator.invoke(input);
            ImmutableImage loadedImage = ImmutableImage.loader().fromBytes(imageMetadata.getData());
            int minSize = Math.min(imageMetadata.getWidth(), imageMetadata.getHeight());
            ImmutableImage image = imageMetadata.getWidth() == imageMetadata.getHeight()
                ? loadedImage
                : loadedImage.cover(minSize, minSize, Position.TopLeft);

            //By spamming simultaneous requests we can race condition the badge upload/deletion and
            // leave on disk old uploaded badge. They will still eventually cleaned by the clean service,
            // however ad DoS is still possible. We pray in mommy Cloudflare's WAF to protect us from this
            // kind of attacks
            MediaData prevMedia = switch (input.type) {
                case BadgeType.BADGE_FURSUIT -> badgeFinder.getMediaDataOfFursuitBadge(input.fursuitId);
                case BadgeType.BADGE_USER -> badgeFinder.getMediaDataOfUserBadge(userId);
            };
            if (prevMedia != null) {
                log.info("[BADGE] User {} is uploading a {} badge: Deleting prev badge id {} from {}",
                        userId, input.type, prevMedia.getId(), prevMedia.getPath());
                //With oncascade this will also set null and clear DB entries for image
                deleteMediaFromDiskAction.invoke(prevMedia, true);
            }

            StoreMediaOnDiskAction.Results res = storeMediaOnDiskAction.invoke(
                    image,
                    input.user,
                    badgeConfig.getStoragePath()
            );

            switch (input.type) {
                case BadgeType.BADGE_FURSUIT: {
                    setFursuitBadgeAction.invoke(input.fursuitId, res.mediaDbId());
                    break;
                }
                case BadgeType.BADGE_USER: {
                    setUserBadgeAction.invoke(userId, res.mediaDbId());
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + input.type);
            }

            return new MediaResponse(res.mediaDbId(), res.relativePath(), imageMetadata.getType());
        } catch (IOException e) {
            log.error("An error occurred while uploading a badge", e);
            throw new ApiException(translationService.error("badge.upload.fail"), ImageCodes.ERROR_WHILE_UPLOADING);
        }
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull MultipartFile image,
            @NotNull BadgeType type,
            @Nullable Long fursuitId,
            @NotNull Event event,
            boolean checkTimeframe
    ) {}
}
