package net.furizon.backend.feature.badge.usecase;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.Position;
import com.sksamuel.scrimage.nio.JpegWriter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.validator.UploadUserBadgeValidator;
import net.furizon.backend.feature.user.action.setBadge.SetUserBadgeAction;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.image.SimpleImageMetadata;
import net.furizon.backend.infrastructure.image.action.StoreMediaOnDiskAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class UploadUserBadgeUsecase implements UseCase<UploadUserBadgeUsecase.Input, BadgeUploadResponse> {

    @NotNull private final BadgeConfig badgeConfig;

    @NotNull private final UploadUserBadgeValidator validator;

    @NotNull private final JpegWriter writer;

    @NotNull private final StoreMediaOnDiskAction storeMediaOnDiskAction;
    @NotNull private final SetUserBadgeAction setUserBadgeAction;

    public UploadUserBadgeUsecase(
        @NotNull BadgeConfig badgeConfig,
        @NotNull UploadUserBadgeValidator validator,
        @NotNull StoreMediaOnDiskAction storeMediaOnDiskAction,
        @NotNull SetUserBadgeAction setUserBadgeAction
    ) {
        this.badgeConfig = badgeConfig;
        this.validator = validator;
        this.storeMediaOnDiskAction = storeMediaOnDiskAction;
        this.setUserBadgeAction = setUserBadgeAction;
        this.writer = new JpegWriter()
            .withCompression(badgeConfig.getJpegQualityThreshold())
            .withProgressive(true);
    }

    @Transactional
    @Override
    public @NotNull BadgeUploadResponse executor(UploadUserBadgeUsecase.@NotNull Input input) {
        try {
            SimpleImageMetadata imageMetadata = validator.invoke(input);
            ImmutableImage loadedImage = ImmutableImage.loader().fromBytes(imageMetadata.getData());
            ImmutableImage image = imageMetadata.getWidth() == imageMetadata.getHeight()
                ? loadedImage
                : loadedImage.cover(imageMetadata.getWidth(), imageMetadata.getWidth(), Position.TopLeft);

            StoreMediaOnDiskAction.Results res = storeMediaOnDiskAction.invoke(
                    image,
                    imageMetadata,
                    input.user,
                    badgeConfig.getStoragePath(),
                    writer
            );

            setUserBadgeAction.invoke(input.user.getUserId(), res.mediaDbId());

            return BadgeUploadResponse.builder()
                .id(1L)
                .relativePath(res.relativePath())
                .build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ApiException("Unable to upload image");
        }
    }

    public record Input(@NotNull FurizonUser user, @NotNull MultipartFile image) {}
}
