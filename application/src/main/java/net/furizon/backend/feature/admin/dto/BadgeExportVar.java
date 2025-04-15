package net.furizon.backend.feature.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BadgeExportVar {
    USER_ID("userId"),
    SERIAL_NUMBER("serialNo"),
    ORDER_CODE("orderCode"),
    FURSONA_NAME("fursonaName"),
    IMAGE_URL("imageUrl"),
    IMAGE_MIME_TYPE("imageMimeType"),
    LOCALES("locales"),
    SPONSORSHIP("sponsorship"),
    FURSUIT_ID("fursuitId"),
    FURSUIT_NAME("fursuitName"),
    FURSUIT_SPECIES("fursuitSpecies"),
    BADGE_LEVEL("badgeLevel")
    ;

    @Getter
    private final @NotNull String varName;

    @Override
    public String toString() {
        return varName;
    }
}
