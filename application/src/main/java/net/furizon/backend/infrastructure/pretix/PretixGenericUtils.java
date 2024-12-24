package net.furizon.backend.infrastructure.pretix;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class PretixGenericUtils {
    public static String buildOrgEventSlug(@NotNull String eventSlug, @NotNull String organizerSlug) {
        return organizerSlug + "/" + eventSlug;
    }

    public static long fromStrPriceToLong(@NotNull String priceStr) {
        String s = priceStr.replaceAll("[^0-9]", "");
        long price = Long.parseLong(s);
        if (!priceStr.contains(".") && !priceStr.contains(",")) {
            price *= 100L;
        }
        return price;
    }
    public static String fromPriceToString(long price, char separator) {
        return String.valueOf(price / 100L) + separator + String.valueOf(price % 100L);
    }

    public static final DateTimeFormatter PRETIX_DATETIME_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .parseLenient()
            .appendOffsetId()
            .parseStrict()
            .optionalStart()
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId()
            .appendLiteral(']')
            .toFormatter();
}
