package net.furizon.backend.feature.badge.validator;

import com.sksamuel.scrimage.format.FormatDetector;
import com.sksamuel.scrimage.metadata.ImageMetadata;
import net.furizon.backend.feature.badge.usecase.UploadUserBadgeUsecase;
import net.furizon.backend.infrastructure.image.SimpleImageMetadata;
import net.furizon.backend.infrastructure.web.ApiCommonErrorCode;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class UploadUserBadgeValidator {
    private static final String IMAGE_WIDTH_TAG = "Image Width";
    private static final String IMAGE_HEIGHT_TAG = "Image Height";
    private static final String IMAGE_TYPE_TAG = "Detected MIME Type";
    private static final String IMAGE_FORMAT_TAG = "Expected File Name Extension";

    public SimpleImageMetadata invoke(@NotNull UploadUserBadgeUsecase.Input input) throws IOException {
        final var imageBytes = input.image().getBytes();
        final var format = FormatDetector.detect(imageBytes);
        if (format.isEmpty()) {
            throw new ApiException("Invalid image", ApiCommonErrorCode.INVALID_INPUT);
        }

        final var metadata = Arrays.stream(ImageMetadata.fromBytes(imageBytes).tags()).toList();
        final var imageWidth = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_WIDTH_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();
        final var imageHeight = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_HEIGHT_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();
        final var imageType = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_TYPE_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();
        final var imageFormat = metadata.stream()
            .filter(it -> it.getName().equals(IMAGE_FORMAT_TAG))
            .findFirst()
            .orElseThrow()
            .getRawValue();

        return SimpleImageMetadata.builder()
            .width(Integer.parseInt(imageWidth))
            .height(Integer.parseInt(imageHeight))
            .type(imageType)
            .format(imageFormat)
            .data(imageBytes)
            .build();
    }
}
