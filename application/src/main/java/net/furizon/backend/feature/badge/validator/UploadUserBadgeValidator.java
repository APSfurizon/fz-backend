package net.furizon.backend.feature.badge.validator;

import com.sksamuel.scrimage.format.Format;
import com.sksamuel.scrimage.format.FormatDetector;
import com.sksamuel.scrimage.metadata.ImageMetadata;
import com.sksamuel.scrimage.metadata.Tag;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.usecase.UploadUserBadgeUsecase;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.image.ImageCodes;
import net.furizon.backend.infrastructure.image.SimpleImageMetadata;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UploadUserBadgeValidator {
    private static final String IMAGE_WIDTH_TAG = "Image Width";
    private static final String IMAGE_HEIGHT_TAG = "Image Height";
    private static final String IMAGE_TYPE_TAG = "Detected MIME Type";
    private static final String IMAGE_FORMAT_TAG = "Expected File Name Extension";

    @NotNull private final BadgeConfig badgeConfig;

    public SimpleImageMetadata invoke(@NotNull UploadUserBadgeUsecase.Input input) throws IOException {
        final byte[] imageBytes = input.image().getBytes();
        if (imageBytes.length > badgeConfig.getMaxSizeBytes()) {
            throw new ApiException("Image is too large to be uploaded", ImageCodes.IMAGE_SIZE_TOO_BIG);
        }

        final Optional<Format> format = FormatDetector.detect(imageBytes);
        if (format.isEmpty()) {
            throw new ApiException("Invalid image", ImageCodes.IMAGE_INVALID);
        }

        final List<Tag> metadata = Arrays.stream(ImageMetadata.fromBytes(imageBytes).tags()).toList();
        final int imageWidth = Integer.parseInt(metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_WIDTH_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue());
        final int imageHeight = Integer.parseInt(metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_HEIGHT_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue());
        final String imageType = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_TYPE_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();
        final String imageFormat = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_FORMAT_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();

        if (imageWidth > badgeConfig.getMaxWidth() || imageHeight > badgeConfig.getMaxHeight()) {
            throw new ApiException("Invalid image dimensions", ImageCodes.IMAGE_DIMENSION_TOO_BIG);
        }

        return SimpleImageMetadata.builder()
            .width(imageWidth)
            .height(imageHeight)
            .type(imageType)
            .format(imageFormat)
            .data(imageBytes)
            .build();
    }
}
