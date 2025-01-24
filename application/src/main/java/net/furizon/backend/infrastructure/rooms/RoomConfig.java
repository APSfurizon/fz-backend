package net.furizon.backend.infrastructure.rooms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
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
        return hotelInternalNameToNames.get(hotelInternalName);
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
