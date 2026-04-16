package net.furizon.backend.infrastructure.pretix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets.Gadget;
import net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets.GadgetManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, String>> CUSTOM_NAMES_TYPE_REFERENCE = new TypeReference<>() {};
    public static @NotNull Map<String, String> convertCustomNames(@Nullable String customNames) {
        if (customNames == null || customNames.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return OBJECT_MAPPER.readValue(customNames, CUSTOM_NAMES_TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException while parsing custom names: {}", customNames, e);
            return Collections.emptyMap();
        }
    }

    private static final TypeReference<List<Gadget>> GADGETS_TYPE_REFERENCE = new TypeReference<>() {};
    public static @Nullable List<Gadget> convertGadgets(@Nullable String gadgets) {
        if (gadgets == null || gadgets.isBlank()) {
            return null;
        }

        try {
            var g = OBJECT_MAPPER.readValue(gadgets, GADGETS_TYPE_REFERENCE);
            return new GadgetManager(g).getGadgets(); //This will sanitize them
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException while parsing gadgets: {}", gadgets, e);
            return null;
        }
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
