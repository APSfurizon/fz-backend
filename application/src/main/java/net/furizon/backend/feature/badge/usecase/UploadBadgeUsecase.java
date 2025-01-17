package net.furizon.backend.feature.badge.usecase;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.feature.badge.validator.UploadUserBadgeValidator;
import net.furizon.backend.feature.user.action.setBadge.SetUserBadgeAction;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.media.SimpleImageMetadata;
import net.furizon.backend.infrastructure.media.action.StoreMediaOnDiskAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
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
public class UploadBadgeUsecase implements UseCase<UploadBadgeUsecase.Input, BadgeUploadResponse> {

    @NotNull private final BadgeConfig badgeConfig;

    @NotNull private final UploadUserBadgeValidator validator;

    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;
    @NotNull private final StoreMediaOnDiskAction storeMediaOnDiskAction;
    @NotNull private final SetUserBadgeAction setUserBadgeAction;

    @NotNull private final BadgeFinder badgeFinder;

    @Override
    @Transactional
    public @NotNull BadgeUploadResponse executor(@NotNull Input input) {
        try {
            long userId = input.user.getUserId();
            if (input.type == BadgeType.BADGE_FURSUIT) {
                Objects.requireNonNull(input.fursuitId);
                //TODO verify user has rights on fursuit
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
                case BadgeType.BADGE_FURSUIT -> null; //TODO
                case BadgeType.BADGE_USER -> badgeFinder.getMediaDataOfUserBadge(userId);
            };
            if (prevMedia != null) {
                log.info("[BADGE] User {} is uploading a {} badge: Deleting prev badge id {} from {}",
                        userId, input.type, prevMedia.getId(), prevMedia.getRelativePath());
                //With oncascade this will also set null and clear DB entries for image
                deleteMediaFromDiskAction.invoke(prevMedia);
            }

            StoreMediaOnDiskAction.Results res = storeMediaOnDiskAction.invoke(
                    image,
                    imageMetadata,
                    input.user,
                    badgeConfig.getStoragePath()
            );

            switch (input.type) {
                case BadgeType.BADGE_FURSUIT: {
                    //TODO
                    break;
                }
                case BadgeType.BADGE_USER: {
                    setUserBadgeAction.invoke(userId, res.mediaDbId());
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + input.type);
            }

            return BadgeUploadResponse.builder()
                .id(1L)
                .relativePath(res.relativePath())
                .build();
        } catch (IOException e) {
            log.error("An error occurred while uploading a badge", e);
            throw new ApiException("Unable to upload image", ImageCodes.ERROR_WHILE_UPLOADING);
        }
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull MultipartFile image,
            @NotNull BadgeType type,
            @Nullable Long fursuitId
    ) {}
}
