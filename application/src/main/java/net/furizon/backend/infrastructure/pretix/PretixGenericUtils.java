package net.furizon.backend.infrastructure.pretix;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class PretixGenericUtils {
    public static String buildOrgEventSlug(String eventSlug, String organizerSlug) {
        return organizerSlug + "/" + eventSlug;
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
