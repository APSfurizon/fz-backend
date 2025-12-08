package net.furizon.backend.infrastructure.rooms;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Slf4j
@ConfigurationProperties(prefix = "room")
public class RoomConfig {
    @Nullable
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final OffsetDateTime roomChangesEndTime;

    @NotNull
    @Getter(AccessLevel.NONE)
    private final String hotelNamesPath;

    @NotNull
    @Getter(AccessLevel.NONE)
    private final Map<String, Map<String, String>> hotelInternalNameToNames;

    public @Nullable Map<String, String> getHotelNames(@NotNull String hotelInternalName) {
        var v = hotelInternalNameToNames.get(hotelInternalName);
        if (v == null) {
            log.error("Hotel internal name '{}' cannot be translated to a display name. "
                    + "Check the hotel-names.json file!",
                      hotelInternalName);
        }
        return v;
    }

    public RoomConfig(@Nullable OffsetDateTime roomChangesEndTime, @NotNull String hotelNamesPath) throws IOException {
        this.roomChangesEndTime = roomChangesEndTime;
        this.hotelNamesPath = hotelNamesPath;

        String json = Files.readString(Paths.get(hotelNamesPath));
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Map<String, String>>> typeRef =
                new TypeReference<Map<String, Map<String, String>>>() {};
        this.hotelInternalNameToNames = mapper.readValue(json, typeRef);
    }
}
