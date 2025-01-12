package net.furizon.backend.feature.badge.usecase;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.Position;
import com.sksamuel.scrimage.nio.JpegWriter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.validator.UploadUserBadgeValidator;
import net.furizon.backend.infrastructure.badge.BadgeConfig;
import net.furizon.backend.infrastructure.image.action.AddMediaAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class UploadUserBadgeUsecase implements UseCase<UploadUserBadgeUsecase.Input, BadgeUploadResponse> {
    private final BadgeConfig badgeConfig;

    private final UploadUserBadgeValidator validator;

    private final JpegWriter writer;

    private final AddMediaAction addMediaAction;

    public UploadUserBadgeUsecase(
        BadgeConfig badgeConfig,
        UploadUserBadgeValidator validator,
        AddMediaAction addMediaAction
    ) {
        this.badgeConfig = badgeConfig;
        this.validator = validator;
        this.addMediaAction = addMediaAction;
        this.writer = new JpegWriter()
            .withCompression(badgeConfig.getJpegQualityThreshold())
            .withProgressive(true);
    }

    @Transactional
    @Override
    public @NotNull BadgeUploadResponse executor(UploadUserBadgeUsecase.@NotNull Input input) {
        try {
            final var imageMetadata = validator.invoke(input);
            final var loadedImage = ImmutableImage.loader().fromBytes(imageMetadata.getData());
            final var image = imageMetadata.getWidth() / imageMetadata.getHeight() == 1
                ? loadedImage
                : loadedImage.cover(imageMetadata.getWidth(), imageMetadata.getWidth(), Position.TopLeft);

            final String filename = UUID.randomUUID() + ".jpg";
            final String userId = String.valueOf(input.user.getUserId());
            final String relativePath = "/%s/%s".formatted(userId, filename);
            final String absolutePath = "%s%s".formatted(badgeConfig.getStoragePath(), relativePath);
            final File userDir = new File("%s/%s".formatted(badgeConfig.getStoragePath(), userId));

            if (!userDir.exists()) {
                userDir.mkdirs();
            }

            addMediaAction.invoke(relativePath, imageMetadata.getType());
            image.output(writer, new File(absolutePath));

            return BadgeUploadResponse.builder()
                .id(1L)
                .relativePath(relativePath)
                .build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ApiException("Unable to upload image");
        }
    }

    public record Input(@NotNull FurizonUser user, @NotNull MultipartFile image) {}
}
